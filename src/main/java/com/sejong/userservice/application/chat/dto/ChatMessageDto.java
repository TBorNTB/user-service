package com.sejong.userservice.application.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private String type;
    private String roomId;
    private String username;
    private String nickname;
    private String content;
    private String imageUrl;

    public static ChatMessageDto from(ChatMessageEvent event){
        return ChatMessageDto.builder()
                .type(event.getType())
                .roomId(event.getRoomId())
                .username(event.getUsername())
                .nickname(event.getNickname())
                .content(event.getContent())
                .imageUrl(event.getImageUrl())
                .build();
    }

    public static ChatMessageDto from(Map<String,Object> payload){
        return ChatMessageDto.builder()
                .type((String) payload.get("type"))
                .roomId((String) payload.get("roomId"))
                .username((String) payload.get("username"))
                .nickname((String) payload.get("nickname"))
                .content((String) payload.get("content"))
                .imageUrl((String) payload.get("imageUrl"))
                .build();
    }

    public static ChatMessageDto createMethod(String newRoomId) {
        return ChatMessageDto.builder()
                .type("CREATED")
                .roomId(newRoomId)
                .build();
    }


    public static ChatMessageDto joinMethod(ChatMessageDto msg) {
        return ChatMessageDto.builder()
                .type("JOIN")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .build();
    }

    public static ChatMessageDto chatMethod(ChatMessageDto msg) {
        return ChatMessageDto.builder()
                .type("CHAT")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .build();
    }

    public static ChatMessageDto chatClose(ChatMessageDto msg) {
        return ChatMessageDto.builder()
                .type("CLOSE")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .build();
    }
}
