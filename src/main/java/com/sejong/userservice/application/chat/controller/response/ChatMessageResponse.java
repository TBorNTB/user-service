package com.sejong.userservice.application.chat.controller.response;

import com.sejong.userservice.core.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {
    private String message;
    private String senderNickname;
    private String senderUsername;
    private LocalDateTime createdAt;
    private String imageUrl;

    public static ChatMessageResponse of(ChatMessage chatMessage, String senderNickname){
        return ChatMessageResponse.builder()
                .message(chatMessage.getContent())
                .senderNickname(senderNickname)
                .senderUsername(chatMessage.getUsername())
                .createdAt(chatMessage.getCreatedAt())
                .imageUrl(chatMessage.getImageUrl())
                .build();
    }
}
