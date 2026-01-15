package com.sejong.userservice.domain.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    private LocalDateTime createdAt;

    public static ChatRoom dmRoom(String roomId) {
        return ChatRoom.builder()
                .roomId(roomId)
                .roomName(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ChatRoom groupRoom(String newRoomId, String roomName) {
        return ChatRoom.builder()
                .roomId(newRoomId)
                .roomName(roomName)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }
}