package com.sejong.userservice.domain.token;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.REFRESH_TOKEN_NOT_FOUND;

import com.sejong.userservice.domain.token.dto.TokenReissueResponse;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token", description = "JWT 토큰 관련 API")
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Operation(summary = "토큰 재발급", description = "만료된 액세스 토큰을 리프레시 토큰을 이용하여 재발급합니다 (회원/관리자 권한 필요)")
    @PostMapping("/reissue")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        String oldAccessToken = jwtUtil.getAccessTokenFromHeader(request);

        if (oldRefreshToken == null) {
            log.error("재발급 시도 실패: 쿠키에 리프레시 토큰이 없습니다.");
            throw new BaseException(REFRESH_TOKEN_NOT_FOUND);
        }

        TokenReissueResponse reissueResponse = tokenService.reissueTokens(oldAccessToken, oldRefreshToken);

        response.addHeader("Authorization", "Bearer " + reissueResponse.getNewAccessToken());
        response.addCookie(reissueResponse.getNewRefreshTokenCookie());

        return new ResponseEntity<>("토큰이 성공적으로 재발급되었습니다.", HttpStatus.OK);
    }
}
