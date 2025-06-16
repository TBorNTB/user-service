package com.sejong.userservice.domain.model;

import java.util.List;
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
    private String name;
    private String email;
    private String encryptPassword;
    private Integer grade;
    private String major;
    private List<String> specialties;  // todo. enum화 하기
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
