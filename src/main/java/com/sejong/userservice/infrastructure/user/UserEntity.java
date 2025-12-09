package com.sejong.userservice.infrastructure.user;

import com.sejong.userservice.core.user.User;
import com.sejong.userservice.domain.rolechange.domain.UserRole;
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
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private Integer generation;

    @Column(nullable = false, length = 50)
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
    private String githubUrl;

    @Column(length = 255)
    private String linkedinUrl;

    @Column(length = 255)
    private String blogUrl;

    @Column(length = 255)
    private String profileImageUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .generation(user.getGeneration())
                .email(user.getEmail())
                .role(user.getRole())
                .realName(user.getRealName())
                .encryptPassword(user.getEncryptPassword())
                .description(user.getDescription())
                .githubUrl(user.getGithubUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .blogUrl(user.getBlogUrl())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(this.id)
                .nickname(this.getNickname())
                .username(this.username)
                .generation(this.generation)
                .email(this.email)
                .encryptPassword(this.encryptPassword)
                .role(this.role)
                .realName(this.realName)
                .description(this.description)
                .blogUrl(this.blogUrl)
                .githubUrl(this.githubUrl)
                .linkedinUrl(this.linkedinUrl)
                .profileImageUrl(this.profileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public void updateUsername() {
        this.username = "tbntb " + this.id;
    }

    public void updateUserRole(String userRole) {
        this.role = UserRole.valueOf(userRole);
    }
}