package com.sejong.userservice.domain.user.domain;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private Integer generation;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String realName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private String encryptPassword;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String techStack;

    @Column(length = 255)
    private String githubUrl;

    @Column(length = 255)
    private String linkedinUrl;

    @Column(length = 255)
    private String blogUrl;

    @Column(length = 255)
    private String profileImageKey;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
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
            .description(joinRequest.getDescription())
            .techStack(joinRequest.getTechStack())
            .githubUrl(joinRequest.getGithubUrl())
            .linkedinUrl(joinRequest.getLinkedinUrl())
            .blogUrl(joinRequest.getBlogUrl())
            .profileImageKey(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public void updateUsername() {
        this.username = "tbntb " + this.id;
    }

    public void updateUserRole(UserRole userRole) {
        this.role = userRole;
    }

    public void updateProfile(UserUpdateRequest updateRequest) {
        if (updateRequest.getEmail() != null) {
            this.email = updateRequest.getEmail();
        }
        if (updateRequest.getRealName() != null) {
            this.realName = updateRequest.getRealName();
        }
        if (updateRequest.getDescription() != null) {
            this.description = updateRequest.getDescription();
        }
        if (updateRequest.getTechStack() != null) {
            this.techStack = updateRequest.getTechStack();
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
            this.profileImageKey = updateRequest.getProfileImageUrl();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newEncryptedPassword) {
        this.encryptPassword = newEncryptedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfileImage(String newImageKey) {
        this.profileImageKey = newImageKey;
    }
}