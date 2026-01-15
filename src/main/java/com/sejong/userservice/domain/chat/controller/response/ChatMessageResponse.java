package com.sejong.userservice.domain.chat.controller.response;

import com.sejong.userservice.domain.chat.domain.ChatMessage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {
    private String message;
    private String senderNickname;
    private String senderUsername;
    private String senderThumbnailUrl;
    private LocalDateTime createdAt;
    private String imageUrl;

    public static ChatMessageResponse of(ChatMessage chatMessage, String senderNickname, String senderThumbnailUrl){
        return ChatMessageResponse.builder()
                .message(chatMessage.getContent())
                .senderNickname(senderNickname)
                .senderUsername(chatMessage.getUsername())
                .senderThumbnailUrl(senderThumbnailUrl)
                .createdAt(chatMessage.getCreatedAt())
                .imageUrl(chatMessage.getImageUrl())
                .build();
    }
}
