package com.sejong.userservice.application.chat.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.service.ChatHandleMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ChatHandleMessageService chatHandleMessageService;
    private final com.sejong.userservice.application.chat.config.ServerIdProvider serverIdProvider;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 수신된 JSON 문자열 → ChatMessage 객체로 변환
            String json = new String(message.getBody());
            ChatMessageDto chatMessage = objectMapper.readValue(json, ChatMessageDto.class);

            log.info("Redis 메시지 수신 - roomId: {}, 발신 serverId: {}, 현재 serverId: {}", chatMessage.getRoomId(),
                    chatMessage.getServerId(), serverIdProvider.getServerId());

            // 같은 서버에서 발행한 메시지는 무시 (이미 로컬 브로드캐스트 완료)
            if (serverIdProvider.getServerId().equals(chatMessage.getServerId())) {
                log.info("같은 서버에서 발행한 메시지 무시 - roomId: {}, serverId: {}", chatMessage.getRoomId(),
                        chatMessage.getServerId());
                return;
            }

            log.info("다른 서버에서 발행한 메시지 브로드캐스트 - roomId: {}, 발신 serverId: {}, 현재 serverId: {}", chatMessage.getRoomId(),
                    chatMessage.getServerId(), serverIdProvider.getServerId());
            chatHandleMessageService.broadcast(chatMessage.getRoomId(), json);
        } catch (Exception e) {
            log.error("RedisSubscriber error", e);
        }
    }
}
