package com.sejong.userservice.domain.like.service;

import com.sejong.userservice.domain.like.repository.LikeRepository;
import com.sejong.userservice.support.common.redis.RedisService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeScheduler {

    private final LikeRepository likeRepository;
    private final RedisService redisService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul") // 매일 오전 3시
    public void runRedisLikeSyncJob() {
        log.info("Redis 좋아요 동기화 작업 시작 - 통계 쿼리 방식");

        // 통계 쿼리로 좋아요 수 조회
        Map<String, Long> likeCountStatistics = likeRepository.getLikeCountStatistics();

        // Redis에 일괄 설정
        likeCountStatistics.forEach(redisService::setCount);

        log.info("Redis 좋아요 동기화 작업 완료: {} 건 처리", likeCountStatistics.size());

        /**
         * TODO: 접근이 잦은 게시물에 대한 전략적 캐싱을 고려해야합니다.(postType, postId, userId)
         *
         * JobParameters jobParameters = new JobParametersBuilder()
         *                     .addLong("timestamp", System.currentTimeMillis()) // 매 실행 시마다 다른 값으로 중복 방지
         *                     .toJobParameters();
         *
         *             jobLauncher.run(postLikeRedisSyncJob, jobParameters);
         */
    }
}
