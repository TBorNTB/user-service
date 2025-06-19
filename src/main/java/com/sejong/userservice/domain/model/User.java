package com.sejong.userservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
        this.updatedAt = LocalDateTime.now(); // 업데이트 시간 갱신
        return this; // 자신을 반환하여 메서드 체이닝 가능
    }
}
