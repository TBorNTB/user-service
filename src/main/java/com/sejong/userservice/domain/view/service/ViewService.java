package com.sejong.userservice.domain.view.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_POST_TYPE_POST_ID;

import com.sejong.userservice.domain.view.domain.View;
import com.sejong.userservice.domain.view.domain.ViewDailyHistory;
import com.sejong.userservice.domain.view.dto.response.ViewCountResponse;
import com.sejong.userservice.domain.view.repository.ViewDailyHistoryRepository;
import com.sejong.userservice.domain.view.repository.ViewJPARepository;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.redis.RedisKeyUtil;
import com.sejong.userservice.support.common.redis.RedisService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewService {
    
    private static final Duration VIEW_TTL = Duration.ofHours(24);
    private static final String VIEW_COUNT_PATTERN = "post:*:view:count";
    
    private final PostInternalFacade postInternalFacade;
    private final RedisService redisService;
    private final ViewJPARepository viewJPARepository;
    private final ViewDailyHistoryRepository viewDailyHistoryRepository;

    @Transactional
    public void updateViewCount(Long postId, PostType postType, Long viewCount) {
        View view = viewJPARepository
                .findByPostTypeAndPostId(postType, postId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_POST_TYPE_POST_ID));
        view.updateViewCount(viewCount);
    }

    public void checkPostExistence(Long postId, PostType postType) {
        postInternalFacade.checkPostExistenceAndOwner(postId, postType);
    }

    @Transactional
    public ViewCountResponse increaseViewCount(Long postId, PostType postType, String clientIp) {
        checkPostExistence(postId, postType);
        
        String ipKey = RedisKeyUtil.viewIpKey(postType, postId, clientIp);
        String viewCountKey = RedisKeyUtil.viewCountKey(postType, postId);

        // cache miss일 때만 DB 조회 (SETNX)
        if (!redisService.hasKey(viewCountKey)) {
            View view = getOrCreateViewEntity(postType, postId);
            redisService.setIfAbsent(viewCountKey, view.getViewCount());
        }

        // SETNX = SET if Not eXists
        boolean isFirstView = redisService.setIfAbsent(ipKey, "check", VIEW_TTL);
        if (!isFirstView) {
            return new ViewCountResponse(redisService.getCount(viewCountKey));
        }

        return new ViewCountResponse(redisService.increment(viewCountKey));
    }

    @Transactional
    public ViewCountResponse getViewCount(Long postId, PostType postType) {
        String viewCountKey = RedisKeyUtil.viewCountKey(postType, postId);

        // cache miss (mysql -> redis)
        if (!redisService.hasKey(viewCountKey)) {
            View view = getOrCreateViewEntity(postType, postId);
            redisService.setIfAbsent(viewCountKey, view.getViewCount());
        }

        Long viewCount = redisService.getCount(viewCountKey);
        return new ViewCountResponse(viewCount);
    }

    private View getOrCreateViewEntity(PostType postType, Long postId) {
        return viewJPARepository.findByPostTypeAndPostId(postType, postId)
                .orElseGet(() -> {
                    try {
                        return viewJPARepository.save(View.of(postType, postId, 0L));
                    } catch (DataIntegrityViolationException e) {
                        // 동시 요청으로 이미 저장됨
                        return viewJPARepository.findByPostTypeAndPostId(postType, postId)
                                .orElseThrow();
                    }
                });
    }

    public ViewCountResponse getAllViewCount(Long startedDay, Long endedDay) {
        LocalDateTime startDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startedDay), 
                ZoneId.systemDefault()
        );
        LocalDateTime endDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(endedDay), 
                ZoneId.systemDefault()
        );
        Long viewCount = viewJPARepository.findByViewAllCount(startDate, endDate);
        return new ViewCountResponse(viewCount);
    }

    public ViewCountResponse getTotalViewCountSince(Long startDateTimestamp) {
        LocalDateTime startDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startDateTimestamp), 
                ZoneId.systemDefault()
        );
        Long viewCount = viewJPARepository.findTotalViewCountSince(startDate);
        return new ViewCountResponse(viewCount);
    }

    @Transactional(readOnly = true)
    public Long calculateTotalViewCount() {
        return viewJPARepository.findAll().stream()
                .mapToLong(View::getViewCount)
                .sum();
    }


    public Long calculateTotalViewCountFromRedis() {
        return redisService.scanAndSum(VIEW_COUNT_PATTERN);
    }

    @Transactional
    public void saveOrUpdateDailyHistory(LocalDate date, Long currentTotalViewCount) {
        ViewDailyHistory existingHistory = viewDailyHistoryRepository.findByDate(date).orElse(null);
        
        Long incrementCount;
        if (existingHistory == null) {
            incrementCount = 0L;
            ViewDailyHistory newHistory = ViewDailyHistory.of(date, currentTotalViewCount, incrementCount);
            viewDailyHistoryRepository.save(newHistory);
        } else {
            Long previousTotalViewCount = existingHistory.getTotalViewCount();
            incrementCount = Math.max(0L, currentTotalViewCount - previousTotalViewCount);

            if (incrementCount > 0) {
                existingHistory.updateViewCount(currentTotalViewCount, incrementCount);
                viewDailyHistoryRepository.save(existingHistory);
            }
        }
    }


    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCount(LocalDate date) {
        Long incrementCount = viewDailyHistoryRepository.findByDate(date)
                .map(ViewDailyHistory::getIncrementCount)
                .orElse(0L);
        return new ViewCountResponse(incrementCount);
    }

    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCountBetween(LocalDate startDate, LocalDate endDate) {
        Long incrementCount = viewDailyHistoryRepository.findIncrementCountBetweenDates(startDate, endDate);
        return new ViewCountResponse(incrementCount);
    }

    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCountSince(LocalDate startDate) {
        Long incrementCount = viewDailyHistoryRepository.findIncrementCountSince(startDate);
        return new ViewCountResponse(incrementCount);
    }
}