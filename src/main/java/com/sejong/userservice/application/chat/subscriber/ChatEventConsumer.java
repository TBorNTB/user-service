package com.sejong.userservice.application.chat.subscriber;

import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.dto.ChatMessageEvent;
import com.sejong.userservice.application.chat.publisher.RedisPublisher;
import com.sejong.userservice.application.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;

    @KafkaListener(
            topics = "chat-message",
            groupId = "chat-message-group"
    )
    public void consume(String message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);
        ChatMessageDto chatMessageDto = ChatMessageDto.from(event);
        chatService.save(chatMessageDto);
        redisPublisher.publish(chatMessageDto);
    }
}
