package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaChatRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.roomId = :roomId")
    List<ChatMessageEntity> findAllChatMessages(@Param("roomId")String roomId);
}
