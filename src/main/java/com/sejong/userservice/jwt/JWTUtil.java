package com.sejong.userservice.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {
    private SecretKey secretKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public JWTUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-time}") long accessTokenExpirationTime,
                   @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTime) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIG.HS256.key().build().getAlgorithm());  // 더 강력한 HMAC SHA-256으로?

        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    /**
     * 토큰 검증
     */
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 토큰이 만료되었을 경우 발생하는 예외 처리
            return true;
        } catch (Exception e) {
            // 다른 예외 발생 시 (예: 토큰 변조), 유효하지 않다고 간주
            return true;
        }
    }

    /**
     * 토큰 발급 : accessToken
     */
    public String createAccessToken(String username, String role) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 발급 : refreshToken
     */
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰을 위한 HTTP Only 쿠키 생성
     */
    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge((int) (refreshTokenExpirationTime / 1000)); // 쿠키 만료 시간 (초 단위)
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가
        // cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/"); // 모든 경로에서 접근 가능하도록 설정
        return cookie;
    }

    /**
     * 요청에서 리프레시 토큰 쿠키 추출
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
