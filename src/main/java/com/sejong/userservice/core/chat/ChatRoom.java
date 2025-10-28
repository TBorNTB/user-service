package com.sejong.userservice.core.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {
    private String roomName;
    private String roomId;

    public static ChatRoom createSingle(String roomId) {
        return ChatRoom.builder()
                .roomName(null)  // 1:1개인방일 경우 카카오톡처럼 상대방 닉네임을 보이기 위해 null처리
                .roomId(roomId)
                .build();
    }

    public static ChatRoom createGroup(String newRoomId, String roomName) {
        return ChatRoom.builder()
                .roomId(newRoomId)
                .roomName(roomName)
                .build();
    }
}
