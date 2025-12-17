package com.sejong.userservice.domain.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="chatroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    private String roomId;   // 일단 기존 String 유지

    private String roomName;

    public static ChatRoom dmRoom(String roomId) {
        return ChatRoom.builder()
                .roomId(roomId)
                .roomName(null)
                .build();
    }

    public static ChatRoom groupRoom(String newRoomId, String roomName) {
        return ChatRoom.builder()
                .roomId(newRoomId)
                .roomName(roomName)
                .build();
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }
}