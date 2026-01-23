package com.sejong.userservice.domain.chat.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomsPageResponse {
    private List<ChatRoomListItemResponse> items;

    // 요청에 사용된 커서 (없으면 null)
    private LocalDateTime cursorAt;
    private String cursorRoomId;

    // 다음 페이지 커서 (없으면 null)
    private LocalDateTime nextCursorAt;
    private String nextCursorRoomId;

    public static ChatRoomsPageResponse of(
            List<ChatRoomListItemResponse> items,
            LocalDateTime cursorAt,
            String cursorRoomId,
            LocalDateTime nextCursorAt,
            String nextCursorRoomId
    ) {
        return ChatRoomsPageResponse.builder()
                .items(items)
                .cursorAt(cursorAt)
                .cursorRoomId(cursorRoomId)
                .nextCursorAt(nextCursorAt)
                .nextCursorRoomId(nextCursorRoomId)
                .build();
    }
}
