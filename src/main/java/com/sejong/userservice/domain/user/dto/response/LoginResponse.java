package com.sejong.userservice.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Todo: 그냥 삭제!
@Schema(description = "로그인 응답")
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse {

    @Schema(description = "응답 메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
}
