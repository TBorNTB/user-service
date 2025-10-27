package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatMessage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="chat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String username;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatMessageEntity from(ChatMessage chatMessage) {
        return ChatMessageEntity.builder()
                .type(chatMessage.getType())
                .username(chatMessage.getUsername())
                .content(chatMessage.getContent())
                .imageUrl(chatMessage.getImageUrl())
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .build();
    }

    public ChatMessage toDomain() {
        return ChatMessage.builder()
                .type(type)
                .username(username)
                .content(content)
                .imageUrl(imageUrl)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
