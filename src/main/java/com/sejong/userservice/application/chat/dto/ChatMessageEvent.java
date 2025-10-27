package com.sejong.userservice.application.chat.dto;

import com.sejong.userservice.core.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEvent {
    private String type;
    private String roomId;
    private String username;
    private String nickname;
    private String content;
    private String imageUrl;

    public static ChatMessageEvent from(ChatMessageDto chatMessageDto) {
        return ChatMessageEvent.builder()
                .type(chatMessageDto.getType())
                .roomId(chatMessageDto.getRoomId())
                .username(chatMessageDto.getUsername())
                .nickname(chatMessageDto.getNickname())
                .content(chatMessageDto.getContent())
                .imageUrl(chatMessageDto.getImageUrl())
                .build();
    }
}
