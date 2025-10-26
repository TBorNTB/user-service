package com.sejong.userservice.application.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 수신된 JSON 문자열 → ChatMessage 객체로 변환
            String json = new String(message.getBody());
            ChatMessageDto chatMessage = objectMapper.readValue(json, ChatMessageDto.class);

            log.info("Received message: {}", chatMessage);
            chatWebSocketHandler.broadcast(chatMessage.getRoomId(), json);
        } catch (Exception e) {
            log.error("RedisSubscriber error", e);
        }
    }
}