package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponse {
    private Long id;
    private String username;
    private String realName;
    private String nickname;
    private String profileImageUrl;
    private String email;

    public static UserSearchResponse from(User user) {
        if (user == null) {
            return null;
        }
        return UserSearchResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .build();
    }
}

