package com.sejong.userservice.domain.chat.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String CHAT_MESSAGE_TOPIC = "chat-message";

    /**
     * Kafka에 발행. partition key = roomId 로 설정하여 방 단위 메시지 순서 보장.
     */
    public void publish(ChatMessageDto chatMessageDto) {
        ChatMessageEvent event = ChatMessageEvent.from(chatMessageDto);
        String json = toJson(event);
        String partitionKey = chatMessageDto.getRoomId();
        kafkaTemplate.send(CHAT_MESSAGE_TOPIC, partitionKey, json);
        log.info("{} type 카프카 발행 (roomId={}, partitionKey={})", event.getType(), partitionKey, partitionKey);
    }

    public String toJson(ChatMessageEvent chatMessageEvent) {
        try {
            return objectMapper.writeValueAsString(chatMessageEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
