package com.sejong.userservice.chat.repository;


import com.sejong.userservice.chat.domain.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.roomId = :roomId")
    List<ChatMessage> findAllChatMessages(@Param("roomId")String roomId);
}
