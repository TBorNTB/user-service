package com.sejong.userservice.application.chat.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SingleRoomResponse {
    private String roomName;
    private String roomId;

    public static SingleRoomResponse of(String roomId, String roomName) {
        return SingleRoomResponse.builder()
                .roomId(roomId)
                .roomName(roomName)
                .build();
    }
}
