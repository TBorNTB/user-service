package com.sejong.userservice.domain.chat.repository;


import com.sejong.userservice.domain.chat.domain.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.roomId = :roomId")
    List<ChatMessage> findAllChatMessages(@Param("roomId")String roomId);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        WHERE cm.chatRoom.roomId = :roomId
          AND cm.content IS NOT NULL
          AND (:cursorId IS NULL OR cm.id < :cursorId)
        ORDER BY cm.id DESC
        """)
    List<ChatMessage> findChatMessagesPage(
        @Param("roomId") String roomId,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );

    @Query("""
        SELECT MAX(cm.id)
        FROM ChatMessage cm
        WHERE cm.chatRoom.roomId = :roomId
          AND cm.type = 'CHAT'
          AND (cm.content IS NOT NULL OR cm.imageUrl IS NOT NULL)
        """)
    Long findLatestReadableChatMessageId(@Param("roomId") String roomId);

    @Query("""
        SELECT COUNT(cm)
        FROM ChatMessage cm
        WHERE cm.chatRoom.roomId = :roomId
          AND cm.type = 'CHAT'
          AND (cm.content IS NOT NULL OR cm.imageUrl IS NOT NULL)
          AND (:afterId IS NULL OR cm.id > :afterId)
          AND cm.username <> :username
        """)
    long countUnreadChatMessages(
        @Param("roomId") String roomId,
        @Param("afterId") Long afterId,
        @Param("username") String username
    );
}
