package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.user.domain.User;
import java.time.LocalDateTime;
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
public class UserRes {
    private Long id;
    private String nickname;
    private String role;
    private String realName;
    private String email;
    private String username;
    private String description;
    private String githubUrl;
    private String linkedinUrl;
    private String blogUrl;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserRes from(User user) {
        if (user == null) {
            return null;
        }
        return UserRes.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .realName(user.getRealName())
                .email(user.getEmail())
                .username(user.getUsername())
                .description(user.getDescription())
                .githubUrl(user.getGithubUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .blogUrl(user.getBlogUrl())
                .profileImageUrl(user.getProfileImageKey())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}


