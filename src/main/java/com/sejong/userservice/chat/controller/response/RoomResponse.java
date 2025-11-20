package com.sejong.userservice.chat.controller.response;

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

    public static RoomResponse quitOf(String roomId) {
        return RoomResponse.builder()
                .roomId(roomId)
                .content("채팅방을 나갔습니다.")
                .build();
    }
}
