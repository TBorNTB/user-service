package com.sejong.userservice.core.user;

import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import com.sejong.userservice.domain.rolechange.domain.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    private static final Logger log = LoggerFactory.getLogger(User.class);
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

    public User approveAs(UserRole newRole, Integer generation) {
        if (this.role != newRole) {
            log.info("권한 변경 발생. " + this.nickname + "의 권한 : " + this.role + " > " + newRole);
            this.role = newRole;
            this.generation = generation;
            this.updatedAt = LocalDateTime.now();
        }
        return this;
    }

    public User getRole(UserRole newRole) {
      if (this.role != newRole) {
        log.info("권한 변경 발생. " + this.nickname + "의 권한 : " + this.role + " > " + newRole);
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
      }
      return this;
    }

    public void updatePassword(String newEncryptedPassword) {
        this.encryptPassword = newEncryptedPassword;
        this.updatedAt = LocalDateTime.now();
        log.info("비밀번호가 변경되었습니다. 사용자: {}", this.email);
    }
}
