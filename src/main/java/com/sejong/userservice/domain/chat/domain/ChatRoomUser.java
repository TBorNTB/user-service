package com.sejong.userservice.domain.chat.domain;

import com.sejong.userservice.domain.chat.constant.ChatRoomUserRole;
import com.sejong.userservice.domain.user.domain.User;
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
    private User user;

    // 방에서의 역할 (일반 참여자 / 방장 등)
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRoomUserRole role;

    // DM 방 제목 등, 유저별로 다른 표시 이름
    @Column(length = 100)
    private String displayName;

    // 마지막으로 읽은 메시지 ID (cursor)
    private Long lastReadMessageId;

    public static ChatRoomUser join(ChatRoom room, User user, ChatRoomUserRole role) {
        ChatRoomUser cru = ChatRoomUser.builder()
                .chatRoom(room)
                .user(user)
                .role(role)
                .displayName(null)
                .lastReadMessageId(null)
                .build();
        return cru;
    }

    public static ChatRoomUser join(ChatRoom room, User user, ChatRoomUserRole role, String displayName) {
        ChatRoomUser cru = ChatRoomUser.builder()
                .chatRoom(room)
                .user(user)
                .role(role)
                .displayName(displayName)
                .lastReadMessageId(null)
                .build();
        return cru;
    }

    public void changeRole(ChatRoomUserRole role) {
        this.role = role;
    }

    public void updateLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}