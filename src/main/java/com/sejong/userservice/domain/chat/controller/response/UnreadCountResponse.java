package com.sejong.userservice.domain.chat.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnreadCountResponse {
    private String roomId;
    private long unreadCount;
    private Long lastReadMessageId;

    public static UnreadCountResponse of(String roomId, long unreadCount, Long lastReadMessageId) {
        return UnreadCountResponse.builder()
                .roomId(roomId)
                .unreadCount(unreadCount)
                .lastReadMessageId(lastReadMessageId)
                .build();
    }
}
