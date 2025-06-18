package com.sejong.userservice.infrastructure.persistence;

import com.sejong.userservice.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "member")
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
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String encryptPassword;

    private Integer grade;

    @Column(length = 100)
    private String major;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_specialties", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "specialty")
    private List<String> specialties;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 도메인 모델 -> 엔티티 변환
    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .encryptPassword(user.getEncryptPassword())
                .grade(user.getGrade())
                .major(user.getMajor())
                .specialties(user.getSpecialties() != null ? List.copyOf(user.getSpecialties()) : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // 엔티티 -> 도메인 모델 변환
    public User toDomain() {
        return User.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .encryptPassword(this.encryptPassword)
                .grade(this.grade)
                .major(this.major)
                .specialties(this.specialties != null ? List.copyOf(this.specialties) : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}