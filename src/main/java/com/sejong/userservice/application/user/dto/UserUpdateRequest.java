package com.sejong.userservice.application.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "Real name cannot be empty")
    private String realName;
    private String description;
    private String githubUrl;
    private String linkedinUrl;
    private String blogUrl;
    private String profileImageUrl;
}