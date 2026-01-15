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
public class ChatRoomListItemResponse {
    private String roomId;
    private String roomName;

    // 최근 채팅 시간 (없으면 null)
    private LocalDateTime lastMessageAt;

    private Long memberCount;
    private Long unreadCount;

    // 최근 메시지 미리보기 (없으면 null)
    private String lastMessage;
    private String lastMessageImageUrl;

    // 최근 메시지 발신자 정보 (없으면 null)
    private String lastSenderUsername;
    private String lastSenderNickname;
    private String lastSenderRealName;
    private String lastSenderThumbnailUrl;

    // 나를 제외한 참여자 목록
    private List<ChatRoomMemberResponse> members;

    public static ChatRoomListItemResponse of(
            String roomId,
            String roomName,
            LocalDateTime lastMessageAt,
            String lastMessage,
            String lastMessageImageUrl,
            String lastSenderUsername,
            String lastSenderNickname,
            String lastSenderRealName,
            String lastSenderThumbnailUrl,
            Long memberCount,
            Long unreadCount,
            List<ChatRoomMemberResponse> members
    ) {
        return ChatRoomListItemResponse.builder()
                .roomId(roomId)
                .roomName(roomName)
                .lastMessageAt(lastMessageAt)
                .lastMessage(lastMessage)
                .lastMessageImageUrl(lastMessageImageUrl)
                .lastSenderUsername(lastSenderUsername)
                .lastSenderNickname(lastSenderNickname)
                .lastSenderRealName(lastSenderRealName)
                .lastSenderThumbnailUrl(lastSenderThumbnailUrl)
                .memberCount(memberCount)
                .unreadCount(unreadCount)
                .members(members)
                .build();
    }
}
