package com.sejong.userservice.domain.chat.repository.projection;

public interface RoomUnreadCountProjection {
    String getRoomId();

    long getUnreadCount();
}
