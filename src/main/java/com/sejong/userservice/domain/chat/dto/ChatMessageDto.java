package com.sejong.userservice.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime createdAt;
    private String serverId; // 메시지를 발행한 서버 ID (중복 방지용)
    private String token; // AUTH 메시지 타입에서 사용하는 토큰 필드

    public static ChatMessageDto from(ChatMessageEvent event){
        return ChatMessageDto.builder()
                .type(event.getType())
                .roomId(event.getRoomId())
                .username(event.getUsername())
                .nickname(event.getNickname())
                .content(event.getContent())
                .imageUrl(event.getImageUrl())
                .createdAt(event.getCreatedAt())
                .serverId(event.getServerId())
                .build();
    }

    public static ChatMessageDto from(Map<String,Object> payload, String username, String nickname){
        return ChatMessageDto.builder()
                .type((String) payload.get("type"))
                .roomId((String) payload.get("roomId"))
                .username(username)
                .nickname(nickname)
                .content((String) payload.get("content"))
                .imageUrl((String) payload.get("imageUrl"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ChatMessageDto joinMethod(ChatMessageDto msg, String serverId) {
        return ChatMessageDto.builder()
                .type("JOIN")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .createdAt(msg.getCreatedAt())
                .serverId(serverId)
                .build();
    }

    public static ChatMessageDto chatMethod(ChatMessageDto msg, String serverId) {
        return ChatMessageDto.builder()
                .type("CHAT")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .createdAt(msg.getCreatedAt())
                .serverId(serverId)
                .build();
    }

    public static ChatMessageDto chatClose(ChatMessageDto msg, String serverId) {
        return ChatMessageDto.builder()
                .type("CLOSE")
                .roomId(msg.getRoomId())
                .username(msg.getUsername())
                .nickname(msg.getNickname())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .createdAt(msg.getCreatedAt())
                .serverId(serverId)
                .build();
    }
}
