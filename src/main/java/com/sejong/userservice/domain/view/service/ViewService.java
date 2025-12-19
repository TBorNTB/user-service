package com.sejong.userservice.domain.view.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_POST_TYPE_POST_ID;

import com.sejong.userservice.domain.view.domain.View;
import com.sejong.userservice.domain.view.dto.response.ViewCountResponse;
import com.sejong.userservice.domain.view.repository.ViewJPARepository;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.redis.RedisKeyUtil;
import com.sejong.userservice.support.common.redis.RedisService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewService {
    
    private static final Duration VIEW_TTL = Duration.ofHours(24);
    
    private final PostInternalFacade postInternalFacade;
    private final RedisService redisService;
    private final ViewJPARepository viewJPARepository;

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

    public ViewCountResponse increaseViewCount(Long postId, PostType postType, String clientIp) {
        checkPostExistence(postId, postType);
        
        String ipKey = RedisKeyUtil.viewIpKey(postType, postId, clientIp);
        String viewCountKey = RedisKeyUtil.viewCountKey(postType, postId);

        // cache miss일 때만 DB 조회 (SETNX)
        if (redisService.hasKey(viewCountKey)) {
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

}