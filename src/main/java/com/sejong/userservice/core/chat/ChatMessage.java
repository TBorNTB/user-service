package com.sejong.userservice.core.chat;

import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String type;
    private String roomId;
    private String username;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatMessage from(ChatMessageDto chatMessageDto) {
        return ChatMessage.builder()
                .type(chatMessageDto.getType())
                .roomId(chatMessageDto.getRoomId())
                .username(chatMessageDto.getUsername())
                .content(chatMessageDto.getContent())
                .imageUrl(chatMessageDto.getImageUrl())
                .createdAt(chatMessageDto.getCreatedAt())
                .updatedAt(chatMessageDto.getCreatedAt())
                .build();
    }
}
