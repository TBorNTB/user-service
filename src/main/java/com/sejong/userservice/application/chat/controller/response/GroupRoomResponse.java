package com.sejong.userservice.application.chat.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupRoomResponse {
    private String roomId;
    private String roomName;

    public static GroupRoomResponse of(String roomId, String roomName) {
        return GroupRoomResponse.builder()
                .roomId(roomId)
                .roomName(roomName)
                .build();
    }
}
