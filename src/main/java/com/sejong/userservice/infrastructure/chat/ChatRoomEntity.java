package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatRoom;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="chatroom")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomEntity {
    @Id
    private String roomId;

    private String roomName;

    public static ChatRoomEntity from(ChatRoom chatRoom) {
        return ChatRoomEntity.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .build();
    }

    public ChatRoom toDomain() {
        return ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .build();
    }
}
