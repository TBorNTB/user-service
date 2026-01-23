package com.sejong.userservice.domain.chat.repository.projection;

public interface RoomCountProjection {
    String getRoomId();

    long getCount();
}
