package com.sejong.userservice.domain.view.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_POST_TYPE_POST_ID;

import com.sejong.userservice.domain.view.domain.View;
import com.sejong.userservice.domain.view.domain.ViewDailyHistory;
import com.sejong.userservice.domain.view.dto.response.ViewCountResponse;
import com.sejong.userservice.domain.view.dto.response.WeeklyViewCountResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    /**
     * MySQL의 모든 View 엔티티의 총 조회수를 계산합니다.
     * @return MySQL에 저장된 총 조회수
     */
    @Transactional(readOnly = true)
    public Long calculateTotalViewCount() {
        return viewJPARepository.findAll().stream()
                .mapToLong(View::getViewCount)
                .sum();
    }

    /**
     * Redis에서 직접 총 조회수를 계산합니다.
     * Redis에 있는 데이터가 가장 최신이므로 이를 사용합니다.
     * @return Redis에 저장된 총 조회수
     */
    public Long calculateTotalViewCountFromRedis() {
        return redisService.scanAndSum(VIEW_COUNT_PATTERN);
    }

    /**
     * 일일 히스토리를 저장하거나 업데이트합니다.
     * 이전 기록과 비교하여 증가량을 계산합니다.
     * 
     * 같은 날짜 내에서는 증가량을 누적하고,
     * 새로운 날짜의 첫 기록일 경우 전날의 마지막 기록과 비교합니다.
     * 
     * @param date 기록할 날짜
     * @param currentTotalViewCount 현재 총 조회수
     */
    @Transactional
    public void saveOrUpdateDailyHistory(LocalDate date, Long currentTotalViewCount) {
        ViewDailyHistory existingHistory = viewDailyHistoryRepository.findByDate(date).orElse(null);
        
        Long incrementCount;
        if (existingHistory == null) {
            // 새로운 날짜의 첫 기록: 전날의 마지막 기록과 비교
            LocalDate previousDate = date.minusDays(1);
            ViewDailyHistory previousHistory = viewDailyHistoryRepository.findByDate(previousDate).orElse(null);
            
            if (previousHistory == null) {
                // 전날 기록도 없으면 첫 기록
                // 이 시점의 총 조회수가 첫 증가량이 됨 (이미 존재하는 조회수)
                incrementCount = currentTotalViewCount;
            } else {
                // 전날의 마지막 기록과 비교하여 증가량 계산
                Long previousTotalViewCount = previousHistory.getTotalViewCount();
                incrementCount = Math.max(0L, currentTotalViewCount - previousTotalViewCount);
            }
            
            ViewDailyHistory newHistory = ViewDailyHistory.of(date, currentTotalViewCount, incrementCount);
            viewDailyHistoryRepository.save(newHistory);
        } else {
            // 같은 날짜의 기존 기록이 있으면 증가량 계산 후 누적
            Long previousTotalViewCount = existingHistory.getTotalViewCount();
            Long newIncrement = Math.max(0L, currentTotalViewCount - previousTotalViewCount);
            
            // 증가량이 있는 경우에만 업데이트 (중복 기록 방지)
            if (newIncrement > 0) {
                // 기존 증가량에 새로운 증가량을 누적
                Long accumulatedIncrement = existingHistory.getIncrementCount() + newIncrement;
                existingHistory.updateViewCount(currentTotalViewCount, accumulatedIncrement);
                viewDailyHistoryRepository.save(existingHistory);
            }
        }
    }

    /**
     * 특정 날짜의 일일 증가량을 조회합니다.
     * @param date 조회할 날짜
     * @return 해당 날짜의 증가량 (없으면 0)
     */
    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCount(LocalDate date) {
        Long incrementCount = viewDailyHistoryRepository.findByDate(date)
                .map(ViewDailyHistory::getIncrementCount)
                .orElse(0L);
        return new ViewCountResponse(incrementCount);
    }

    /**
     * 기간 내 총 증가량 합계를 조회합니다.
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 기간 내 증가량 합계
     */
    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCountBetween(LocalDate startDate, LocalDate endDate) {
        Long incrementCount = viewDailyHistoryRepository.findIncrementCountBetweenDates(startDate, endDate);
        return new ViewCountResponse(incrementCount);
    }

    /**
     * 특정 날짜부터의 총 증가량 합계를 조회합니다.
     * @param startDate 시작 날짜
     * @return 시작 날짜부터의 증가량 합계
     */
    @Transactional(readOnly = true)
    public ViewCountResponse getDailyViewCountSince(LocalDate startDate) {
        Long incrementCount = viewDailyHistoryRepository.findIncrementCountSince(startDate);
        return new ViewCountResponse(incrementCount);
    }

    /**
     * 주간 방문자 수를 조회합니다.
     * 저번 주부터 시작해서 한 달간(4주)의 주간 통계를 반환합니다.
     * 각 주는 [월, 화, 수, 목, 금, 토, 일] 순서로 배열됩니다.
     * 
     * @return 1주치 데이터, 각 주는 [월, 화, 수, 목, 금, 토, 일] 순서
     *         예: [[저번주 월~일]]
     */
    @Transactional(readOnly = true)
    public WeeklyViewCountResponse getWeeklyViewCount() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        
        // 저번 주 월요일 계산 (오늘이 속한 주의 월요일에서 7일 전)
        int daysFromMonday = (today.getDayOfWeek().getValue() - 1) % 7;
        LocalDate thisWeekMonday = today.minusDays(daysFromMonday);
        LocalDate lastWeekMonday = thisWeekMonday.minusDays(7);
        
        // 저번 주 월요일부터 4주간(28일)의 데이터 조회
        LocalDate startDate = lastWeekMonday;
        LocalDate endDate = startDate.plusDays(6);
        
        // 기간 내 모든 기록 조회
        List<ViewDailyHistory> histories = viewDailyHistoryRepository
                .findByDateBetweenOrderByDate(startDate, endDate);
        
        // 날짜별로 맵 생성 (빠른 조회를 위해)
        Map<LocalDate, Long> countMap = histories.stream()
                .collect(Collectors.toMap(
                        ViewDailyHistory::getDate,
                        ViewDailyHistory::getIncrementCount
                ));
        
        // 1주치 데이터 생성
        List<List<Long>> weeklyData = new ArrayList<>();
        for (int week = 0; week < 1; week++) {
            List<Long> weekData = new ArrayList<>();
            LocalDate weekMonday = startDate.plusDays(week * 7);
            
            // 각 주의 월~일 데이터
            for (int day = 0; day < 7; day++) {
                LocalDate date = weekMonday.plusDays(day);
                Long count = countMap.getOrDefault(date, 0L);
                weekData.add(count);
            }
            weeklyData.add(weekData);
        }
        
        return WeeklyViewCountResponse.builder()
                .weeklyData(weeklyData)
                .build();
    }
}