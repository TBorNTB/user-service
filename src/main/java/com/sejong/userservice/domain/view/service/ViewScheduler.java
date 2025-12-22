package com.sejong.userservice.domain.view.service;

import com.sejong.userservice.domain.view.kafka.ViewEventPublisher;
import com.sejong.userservice.support.common.constants.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewScheduler {

    private static final String VIEW_COUNT_PATTERN = "post:*:view:count";
    private static final int SCAN_COUNT = 100;
    private static final int KEY_PARTS_LENGTH = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ViewService viewService;
    private final ViewEventPublisher viewEventPublisher;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncViewCount() {
        log.info("Redis 조회수 MySQL 동기화 배치 작업 시작");
        int totalProcessed = scanAndSyncViewCounts();
        log.info("Redis 조회수 MySQL 동기화 배치 작업 완료: {} 건 처리", totalProcessed);
    }

    private int scanAndSyncViewCounts() {
        int totalProcessed = 0;

        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(VIEW_COUNT_PATTERN).count(SCAN_COUNT).build())) {

            while (cursor.hasNext()) {
                String key = cursor.next();
                syncSingleViewCount(key);
                totalProcessed++;
            }
        }

        return totalProcessed;
    }

    private void syncSingleViewCount(String redisKey) {
        String[] keyParts = redisKey.split(":");
        if (keyParts.length != KEY_PARTS_LENGTH) {
            log.warn("유효하지 않은 Redis 키 형식: {}", redisKey);
            return;
        }

        PostType postType = PostType.valueOf(keyParts[1]);
        Long postId = Long.valueOf(keyParts[2]);

        String value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return;
        }

        Long viewCount = Long.parseLong(value);
        viewService.updateViewCount(postId, postType, viewCount);
        viewEventPublisher.publish(postType, postId ,viewCount);
        log.debug("조회수 동기화 완료: postType={}, postId={}, viewCount={}", postType, postId, viewCount);
    }
}
