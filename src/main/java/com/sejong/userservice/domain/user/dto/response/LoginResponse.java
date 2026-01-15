package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "로그인 응답")
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LoginResponse {

    private Long id;

    private String username;

    @Schema(description = "응답 메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    private String nickname;

    private UserRole role;

    private String profileImageUrl;

    public static LoginResponse from(User user, String message, String accessToken, String refreshToken) {
        return LoginResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .message(message)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .nickname(user.getNickname())
            .role(user.getRole())
            .profileImageUrl(user.getProfileImageUrl())
            .build();
    }
}
