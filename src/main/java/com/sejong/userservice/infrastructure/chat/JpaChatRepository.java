package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<ChatMessageEntity, Long> {
}
