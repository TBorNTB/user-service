package com.sejong.userservice.domain.like.service;

import com.sejong.userservice.domain.like.domain.Like;
import com.sejong.userservice.domain.like.domain.LikeStatus;
import com.sejong.userservice.domain.like.dto.response.LikeCountRes;
import com.sejong.userservice.domain.like.dto.response.LikeRes;
import com.sejong.userservice.domain.like.repository.LikeRepository;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.kafka.EventPublisher;
import com.sejong.userservice.support.common.redis.RedisKeyUtil;
import com.sejong.userservice.support.common.redis.RedisService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostInternalFacade postInternalFacade;
    private final RedisService redisService;
    private final EventPublisher postlikeEventPublisher;

    @Transactional
    public LikeRes toggleLike(String username, Long postId, PostType postType) {
        String ownerUsername = postInternalFacade.checkPostExistenceAndOwner(postId, postType);
        log.info("유저이름 : {}", ownerUsername);

        Like like = Like.of(username, postId, postType, LocalDateTime.now());
        LikeStatus toggleResult = doToggleLike(like);

        if (toggleResult.equals(LikeStatus.LIKED)) {
            Long count = redisService.increment(RedisKeyUtil.likeCountKey(postType, postId));
            postlikeEventPublisher.publishLike(like, count);
            postlikeEventPublisher.publishLikedAlarm(like, ownerUsername);
            return LikeRes.of(LikeStatus.LIKED, count);
        } else {
            Long count = redisService.decrement(RedisKeyUtil.likeCountKey(postType, postId));
            postlikeEventPublisher.publishLike(like, count);
            return LikeRes.of(LikeStatus.UNLIKED, count);
        }
    }

    private LikeStatus doToggleLike(Like like) {
        try {
            Optional<Like> postLikeEntity = likeRepository.findByUsernameAndPostIdAndPostType(
                    like.getUsername(), like.getPostId(), like.getPostType());

            if (postLikeEntity.isPresent()) {
                likeRepository.deleteById(postLikeEntity.get().getId());
                return LikeStatus.UNLIKED;
            }

            likeRepository.save(like);
            return LikeStatus.LIKED;

        } catch (DataIntegrityViolationException e) {
            // Unique 제약 조건 위반 시 - 이미 다른 요청이 저장했다는 의미 -> 다시 조회해서 삭제 처리
            Optional<Like> existingEntity = likeRepository.findByUsernameAndPostIdAndPostType(
                    like.getUsername(), like.getPostId(), like.getPostType());
            existingEntity.ifPresent(postLikeEntity -> likeRepository.deleteById(postLikeEntity.getId()));
            return LikeStatus.UNLIKED;
        }
    }

    @Transactional(readOnly = true)
    public LikeRes getLikeStatus(String username, Long postId, PostType postType) {
        String redisKey = RedisKeyUtil.likeCountKey(postType, postId);
        boolean liked = likeRepository.existsByUsernameAndPostIdAndPostType(username, postId, postType);
        Long likeCount = redisService.getCount(redisKey);
        if (liked) {
            return LikeRes.of(LikeStatus.LIKED, likeCount);
        } else {
            return LikeRes.of(LikeStatus.UNLIKED, likeCount);
        }
    }

    @Transactional(readOnly = true)
    public LikeCountRes getLikeCount(Long postId, PostType postType) {
        String redisKey = RedisKeyUtil.likeCountKey(postType, postId);
        long likeCount = redisService.getCount(redisKey);
        return new LikeCountRes(likeCount);
    }
}
