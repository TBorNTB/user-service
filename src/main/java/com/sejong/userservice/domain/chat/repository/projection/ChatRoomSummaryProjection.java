package com.sejong.userservice.domain.chat.repository.projection;

import java.time.LocalDateTime;

public interface ChatRoomSummaryProjection {
    String getRoomId();

    String getRoomName();

    String getDisplayName();

    LocalDateTime getLastMessageAt();

    // 정렬/페이지네이션용. 마지막 메시지 없으면 방 생성시간.
    LocalDateTime getActivityAt();

    String getLastMessage();

    String getLastMessageImageUrl();

    String getLastSenderUsername();
}
