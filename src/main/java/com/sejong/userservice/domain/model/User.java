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
}
