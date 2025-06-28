package com.sejong.userservice.domain.model;

import com.sejong.userservice.api.controller.dto.JoinRequest;
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
    private String role;
    private String realName;
    private String email;
    private Integer grade;
    private String major;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User updateProfile(String realName, String email, Integer grade, String major) {
        // null 체크를 통해 전달된 값만 업데이트하도록 함
        if (realName != null) {
            this.realName = realName;
        }
        if (email != null) {
            this.email = email;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (major != null) {
            this.major = major;
        }
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public static User from(JoinRequest joinRequest, String encryptPassword) {
        return User.builder()
                .username(joinRequest.getUsername())
                .encryptPassword(encryptPassword)
                .role("BASIC")  // todo. 요구사항대로 role 수정
                .realName(joinRequest.getRealName())
                .email(joinRequest.getEmail())
                .grade(joinRequest.getGrade())
                .major(joinRequest.getMajor())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
