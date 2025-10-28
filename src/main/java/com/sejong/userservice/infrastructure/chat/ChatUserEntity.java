package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="chatuser")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;

    @Column(nullable = false)
    private String roomId;

    public static ChatUserEntity from(ChatUser chatUser){
        return ChatUserEntity.builder()
                .username(chatUser.getUsername())
                .roomId(chatUser.getRoomId())
                .build();
    }

    public ChatUser toDomain(){
        return ChatUser.builder()
                .username(username)
                .roomId(roomId)
                .build();
    }
}
