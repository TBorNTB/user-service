package com.sejong.userservice.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    @NotBlank(message = "Username cannot be empty")
    private String nickname;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @NotBlank(message = "Real name cannot be empty")
    private String realName;

    @NotBlank(message = "Email cannot be empty")
    @Schema(description = "이메일", example = "tbntb@gmail.com")
    private String email;

    private String description;
    private String githubUrl;
    private String linkedinUrl;
    private String blogUrl;

}
