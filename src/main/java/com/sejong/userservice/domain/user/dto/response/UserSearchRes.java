package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRes {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String email;
    private String profileImageUrl;

    public static UserSearchRes from(User user) {
        if (user == null) {
            return null;
        }
        return UserSearchRes.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}

