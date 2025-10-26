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
    private final ChatWebSocketHandler webSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageDto chatMessage = objectMapper.readValue(message.getBody(), ChatMessageDto.class);
            String roomId = chatMessage.getRoomId();
            log.info("Received message for room {}: {}", roomId, chatMessage.getContent());
            String json = objectMapper.writeValueAsString(chatMessage);
            // roomId에 해당하는 세션만 브로드캐스트
            webSocketHandler.broadcast(roomId, json);

        } catch (Exception e) {
            log.error("RedisSubscriber error", e);
        }
    }
}
