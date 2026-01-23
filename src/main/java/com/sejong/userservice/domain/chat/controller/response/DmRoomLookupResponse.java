package com.sejong.userservice.domain.chat.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DmRoomLookupResponse {
    private String roomId;

    public static DmRoomLookupResponse of(String roomId) {
        return DmRoomLookupResponse.builder()
                .roomId(roomId)
                .build();
    }
}
