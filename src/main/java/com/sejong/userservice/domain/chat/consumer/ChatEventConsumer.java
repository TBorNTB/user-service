package com.sejong.userservice.domain.chat.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageEvent;
import com.sejong.userservice.domain.chat.service.ChatHandleMessageService;
import com.sejong.userservice.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 단일 흐름: 저장과 브로드캐스트를 모두 Kafka Consumer로 처리.
 * - chat-persistence: 한 인스턴스만 CHAT 메시지 DB 저장 (순서·유실 방지)
 * - chat-broadcast-${instanceId}: 인스턴스마다 고유 그룹 → 모든 인스턴스가 수신 후 로컬 WS 세션에만 브로드캐스트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatService chatService;
    private final ChatHandleMessageService chatHandleMessageService;
    private final ObjectMapper objectMapper;

    @Value("${chat.kafka.persistence.max-attempts:3}")
    private int persistenceMaxAttempts;

    @Value("${chat.kafka.persistence.retry-delay-ms:200}")
    private long persistenceRetryDelayMs;

    @Value("${chat.kafka.broadcast.max-attempts:3}")
    private int broadcastMaxAttempts;

    @Value("${chat.kafka.broadcast.retry-delay-ms:100}")
    private long broadcastRetryDelayMs;

    /**
     * 한 인스턴스만 저장 (같은 groupId → 파티션별 1 consumer).
     * 실패 시 재시도. 최종 실패 시 예외 전파 → offset 미커밋 → Kafka가 같은 메시지 재전달.
     */
    @KafkaListener(
            topics = "chat-message",
            groupId = "chat-persistence"
    )
    public void persist(String message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);
        ChatMessageDto dto = ChatMessageDto.from(event);
        if (!"CHAT".equals(dto.getType())) {
            return;
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= persistenceMaxAttempts; attempt++) {
            try {
                chatService.save(dto);
                log.debug("CHAT 메시지 저장 완료 roomId={}", dto.getRoomId());
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < persistenceMaxAttempts) {
                    log.warn("DB 저장 실패 (시도 {}/{}), {}ms 후 재시도 roomId={}",
                            attempt, persistenceMaxAttempts, persistenceRetryDelayMs * attempt, dto.getRoomId(), e);
                    try {
                        Thread.sleep(persistenceRetryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("DB 저장 재시도 대기 중 인터럽트 roomId={}", dto.getRoomId(), ie);
                        throw new RuntimeException("persist interrupted", ie);
                    }
                }
            }
        }
        log.error("DB 저장 최종 실패 ({}회 시도) roomId={}. offset 미커밋으로 재전달 예정.",
                persistenceMaxAttempts, dto.getRoomId(), lastException);
        throw new RuntimeException("chat persist failed after " + persistenceMaxAttempts + " attempts", lastException);
    }

    /**
     * 인스턴스마다 수신 후 로컬 WebSocket 세션에만 브로드캐스트 (groupId 인스턴스별 상이).
     * 실패 시 일시적 오류를 위해 재시도. 최종 실패해도 메시지는 이미 DB에 있으므로 로그만 하고 계속.
     */
    @KafkaListener(
            topics = "chat-message",
            groupId = "${chat.kafka.broadcast-group-id:chat-broadcast-local}"
    )
    public void broadcast(String message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);
        ChatMessageDto dto = ChatMessageDto.from(event);
        String json;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.warn("브로드캐스트 직렬화 실패 roomId={}", dto.getRoomId(), e);
            return;
        }

        String roomId = dto.getRoomId();
        Exception lastException = null;
        for (int attempt = 1; attempt <= broadcastMaxAttempts; attempt++) {
            try {
                chatHandleMessageService.broadcast(roomId, json);
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < broadcastMaxAttempts) {
                    log.warn("브로드캐스트 실패 (시도 {}/{}), {}ms 후 재시도 roomId={}",
                            attempt, broadcastMaxAttempts, broadcastRetryDelayMs, roomId, e);
                    try {
                        Thread.sleep(broadcastRetryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("브로드캐스트 재시도 대기 중 인터럽트", ie);
                        break;
                    }
                }
            }
        }
        log.error("브로드캐스트 최종 실패 ({}회 시도) roomId={}. 메시지는 DB에 저장되어 있으므로 클라이언트는 채팅 내역 조회로 복구 가능.",
                broadcastMaxAttempts, roomId, lastException);
    }
}
