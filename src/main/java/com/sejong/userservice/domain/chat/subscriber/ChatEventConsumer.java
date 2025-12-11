package com.sejong.userservice.domain.chat.subscriber;

import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageEvent;
import com.sejong.userservice.domain.chat.publisher.RedisPublisher;
import com.sejong.userservice.domain.chat.service.ChatService;
import com.sejong.userservice.support.config.ServerIdProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;
    private final ServerIdProvider serverIdProvider;

    @KafkaListener(
            topics = "chat-message",
            groupId = "chat-message-group"
    )
    public void consume(String message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);
        ChatMessageDto chatMessageDto = ChatMessageDto.from(event);
        chatService.save(chatMessageDto);
        // 본인 서버가 아니면 거르면 됨.
        if (!serverIdProvider.getServerId().equals(chatMessageDto.getServerId())) {
            redisPublisher.publish(chatMessageDto);
        }
    }
}
