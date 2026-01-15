package com.sejong.userservice.domain.chat.controller.response;

import com.sejong.userservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomMemberResponse {
    private String username;
    private String nickname;
    private String realName;
    private String thumbnailUrl;

    public static ChatRoomMemberResponse of(User user) {
        return ChatRoomMemberResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .thumbnailUrl(user.getProfileImageUrl())
                .build();
    }
}
