package com.sejong.userservice.application.chat.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessageDto message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(topic.getTopic(), json);
        } catch (Exception e) {
            throw new RuntimeException("Redis publish failed", e);
        }
    }
}