package com.sejong.userservice.core.user;

import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private Long id;
    private String username;
    private String encryptPassword;
    private UserRole role;
    private String realName;
    private String description;
    private String githubUrl;
    private String linkedinUrl;
    private String blogUrl;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User updateProfile(UserUpdateRequest updateRequest) {
        if (updateRequest.getRealName() != null) {
            this.realName = updateRequest.getRealName();
        }
        if (updateRequest.getDescription() != null) {
            this.description = updateRequest.getDescription();
        }
        if (updateRequest.getGithubUrl() != null) {
            this.githubUrl = updateRequest.getGithubUrl();
        }
        if (updateRequest.getLinkedinUrl() != null) {
            this.linkedinUrl = updateRequest.getLinkedinUrl();
        }
        if (updateRequest.getBlogUrl() != null) {
            this.blogUrl = updateRequest.getBlogUrl();
        }
        if (updateRequest.getProfileImageUrl() != null) {
            this.profileImageUrl = updateRequest.getProfileImageUrl();
        }
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public static User from(JoinRequest joinRequest, String encryptPassword) {
        return User.builder()
                .username(joinRequest.getUsername())
                .encryptPassword(encryptPassword)
                .role(UserRole.MEMBER)
                .realName(joinRequest.getRealName())
                .description(null)
                .githubUrl(null)
                .linkedinUrl(null)
                .blogUrl(null)
                .profileImageUrl(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
