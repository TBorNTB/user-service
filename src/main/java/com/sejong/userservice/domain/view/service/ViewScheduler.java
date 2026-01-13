package com.sejong.userservice.domain.view.service;

import com.sejong.userservice.domain.view.kafka.ViewEventPublisher;
import com.sejong.userservice.support.common.constants.PostType;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Service
@RequiredArgsConstructor
@RestController("/viewScheduler")
public class ViewScheduler {

    private static final String VIEW_COUNT_PATTERN = "post:*:view:count";
    private static final int SCAN_COUNT = 100;
    private static final int KEY_PARTS_LENGTH = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ViewService viewService;
    private final ViewEventPublisher viewEventPublisher;

    @GetMapping("/consistent/view/test")
    @Transactional
    public String consistentViewTest() {
        // Redis → MySQL 동기화
        int totalProcessed = scanAndSyncViewCounts();
        
        // 일일 히스토리 기록
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Long totalViewCount = viewService.calculateTotalViewCount();
        viewService.saveOrUpdateDailyHistory(today, totalViewCount);
        
        return String.format("동기화 성공 - 처리 건수: %d, 오늘(%s) 총 조회수: %d 회", 
                totalProcessed, today, totalViewCount);
    }

//    /**
//     * Redis → MySQL 동기화 (하루에 한 번, 새벽 3시)
//     * 데이터 영속성을 위해 주기적으로 동기화
//     */
//    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
//    @Transactional
//    public void syncViewCount() {
//        log.info("Redis 조회수 MySQL 동기화 배치 작업 시작");
//        int totalProcessed = scanAndSyncViewCounts();
//        log.info("Redis 조회수 MySQL 동기화 배치 작업 완료: {} 건 처리", totalProcessed);
//    }

    /**
     * 일일 총 조회수 히스토리 기록 (1시간마다)
     * 대시보드 실시간성을 위해 더 자주 기록
     * 
     * Cron 표현식: "0 0 * * * *" = 매 시간 정각
     * 예: 00:00, 01:00, 02:00, ..., 23:00
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    @Transactional
    public void recordDailyViewHistory() {
        log.debug("일일 조회수 히스토리 기록 시작");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Long totalViewCount = viewService.calculateTotalViewCount();
        viewService.saveOrUpdateDailyHistory(today, totalViewCount);
        log.info("오늘({}) 총 조회수 기록 완료: {} 회", today, totalViewCount);
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
