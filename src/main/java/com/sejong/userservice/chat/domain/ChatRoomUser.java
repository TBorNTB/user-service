package com.sejong.userservice.chat.domain;

import com.sejong.userservice.chat.constant.ChatRoomUserRole;
import com.sejong.userservice.infrastructure.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 방인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    private ChatRoom chatRoom;

    // 어떤 유저인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 방에서의 역할 (일반 참여자 / 방장 등)
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRoomUserRole role;

    public static ChatRoomUser join(ChatRoom room, UserEntity user, ChatRoomUserRole role) {
        ChatRoomUser cru = ChatRoomUser.builder()
                .chatRoom(room)
                .user(user)
                .role(role)
                .build();
        return cru;
    }

    public void changeRole(ChatRoomUserRole role) {
        this.role = role;
    }
}