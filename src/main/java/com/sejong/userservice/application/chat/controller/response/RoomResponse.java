package com.sejong.userservice.application.chat.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponse {
    private String roomId;
    private String content;

    public static RoomResponse deleteOf(String roomId) {
        return RoomResponse.builder()
                .roomId(roomId)
                .content("채팅방을 나갔습니다.")
                .build();
    }
}
