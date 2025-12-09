package com.sejong.userservice.domain.user.domain;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
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
    private String nickname;
    private Integer generation;
    private String email;
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

    public static User from(JoinRequest joinRequest, String encryptPassword) {
        return User.builder()
                .generation(null)
                .nickname(joinRequest.getNickname())
                .email(joinRequest.getEmail())
                .username(null)
                .encryptPassword(encryptPassword)
                .role(UserRole.GUEST)
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

    public User updateProfile(UserUpdateRequest updateRequest) {
        if (updateRequest.getEmail() != null) {
            this.email = updateRequest.getEmail();
        }
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

    public void updatePassword(String newEncryptedPassword) {
        this.encryptPassword = newEncryptedPassword;
        this.updatedAt = LocalDateTime.now();
    }
}
