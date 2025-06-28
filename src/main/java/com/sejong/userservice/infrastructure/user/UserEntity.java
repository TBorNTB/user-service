package com.sejong.userservice.infrastructure.user;

import com.sejong.userservice.core.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 50)
    private String realName;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = true, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String encryptPassword;

    private Integer grade;

    @Column(length = 100)
    private String major;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .realName(user.getRealName())
                .email(user.getEmail())
                .encryptPassword(user.getEncryptPassword())
                .grade(user.getGrade())
                .major(user.getMajor())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(this.id)
                .username(this.getUsername())
                .role(this.getRole())
                .realName(this.realName)
                .email(this.email)
                .encryptPassword(this.encryptPassword)
                .grade(this.grade)
                .major(this.major)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}