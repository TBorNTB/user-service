package com.sejong.userservice.chat.subscriber;

import com.sejong.userservice.chat.dto.ChatMessageDto;
import com.sejong.userservice.chat.dto.ChatMessageEvent;
import com.sejong.userservice.chat.publisher.RedisPublisher;
import com.sejong.userservice.chat.service.ChatService;
import com.sejong.userservice.config.ServerIdProvider;
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
