package com.sejong.userservice.application.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "로그인 요청")
@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {

    @Schema(description = "이메일", example = "tbntb@gmail.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    private String password;
}