package com.sejong.userservice.application.chat.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.dto.ChatMessageEvent;
import com.sejong.userservice.core.chat.ChatMessage;
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

    public void publish(ChatMessageDto chatMessageDto) {
        ChatMessageEvent event = ChatMessageEvent.from(chatMessageDto);
        String json = toJson(event);
        String key = CHAT_MESSAGE_TOPIC + event.getRoomId() + event.getUsername();
        kafkaTemplate.send(CHAT_MESSAGE_TOPIC, key, json);
        log.info("{} type 카프카 발행", event.getType());
    }

    public String toJson(ChatMessageEvent chatMessageEvent) {
        try {
            return objectMapper.writeValueAsString(chatMessageEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
