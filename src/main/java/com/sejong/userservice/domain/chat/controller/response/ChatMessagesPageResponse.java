package com.sejong.userservice.domain.chat.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessagesPageResponse {
    private List<ChatMessageResponse> items;

    // 요청에 사용된 cursorId (없으면 null)
    private Long cursorId;

    // 다음(더 과거) 페이지를 가져오기 위한 cursorId
    // 다음 요청 시 cursorId=<nextCursorId> 로 호출
    private Long nextCursorId;

    public static ChatMessagesPageResponse of(List<ChatMessageResponse> items, Long cursorId, Long nextCursorId) {
        return ChatMessagesPageResponse.builder()
                .items(items)
                .cursorId(cursorId)
                .nextCursorId(nextCursorId)
                .build();
    }
}
