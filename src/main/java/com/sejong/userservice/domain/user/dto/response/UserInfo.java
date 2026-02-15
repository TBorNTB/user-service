package com.sejong.userservice.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    private String username;
    private String nickname;
    private String realName;
    private String profileImageUrl;

    public static UserInfo from(String username, UserNameInfo userNameInfo) {
        if (userNameInfo == null) {
            return UserInfo.builder()
                    .username(username)
                    .nickname(null)
                    .realName(null)
                    .profileImageUrl(null)
                    .build();
        }
        return UserInfo.builder()
                .username(username)
                .nickname(userNameInfo.nickname())
                .realName(userNameInfo.realName())
                .profileImageUrl(userNameInfo.profileImageUrl())
                .build();
    }

    public static UserInfo missing(String username) {
        return UserInfo.builder()
                .username(username)
                .nickname(null)
                .realName(null)
                .profileImageUrl(null)
                .build();
    }
}
