package com.sejong.userservice.domain.chat.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageEvent;
import com.sejong.userservice.domain.chat.service.ChatHandleMessageService;
import com.sejong.userservice.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /** 한 인스턴스만 저장 (같은 groupId → 파티션별 1 consumer). */
    @KafkaListener(
            topics = "chat-message",
            groupId = "chat-persistence"
    )
    public void persist(String message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);
        ChatMessageDto dto = ChatMessageDto.from(event);
        if ("CHAT".equals(dto.getType())) {
            chatService.save(dto);
            log.debug("CHAT 메시지 저장 완료 roomId={}", dto.getRoomId());
        }
    }

    /** 인스턴스마다 수신 후 로컬 WebSocket 세션에만 브로드캐스트 (groupId 인스턴스별 상이). */
    @KafkaListener(
            topics = "chat-message",
            groupId = "${chat.kafka.broadcast-group-id:chat-broadcast-local}"
    )
    public void broadcast(String message) {
        try {
            ChatMessageEvent event = ChatMessageEvent.from(message);
            ChatMessageDto dto = ChatMessageDto.from(event);
            String json = objectMapper.writeValueAsString(dto);
            chatHandleMessageService.broadcast(dto.getRoomId(), json);
        } catch (JsonProcessingException e) {
            log.warn("브로드캐스트 직렬화 실패", e);
        } catch (Exception e) {
            log.warn("브로드캐스트 실패", e);
        }
    }
}
