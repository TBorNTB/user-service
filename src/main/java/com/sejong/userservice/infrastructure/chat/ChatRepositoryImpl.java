package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatMessage;
import com.sejong.userservice.core.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final JpaChatRepository jpaChatRepository;

    @Override
    public void save(ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.from(chatMessage);
        jpaChatRepository.save(entity);
        log.info("db 저장 성공~");
    }
}
