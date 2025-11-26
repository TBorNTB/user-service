package com.sejong.userservice.chat.domain;


import com.sejong.userservice.chat.dto.ChatMessageDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chat",
        indexes = {
                @Index(name = "idx_room_created", columnList ="roomId, createdAt DESC")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;
    private String type;
    private String username;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatMessage from(ChatMessageDto dto, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .type(dto.getType())
                .chatRoom(chatRoom)
                .username(dto.getUsername())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getCreatedAt())
                .build();
    }
}
