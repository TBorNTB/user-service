package com.sejong.userservice.infrastructure.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
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
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                SIG.HS256.key().build().getAlgorithm());

        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    /**
     * 토큰 검증
     */
    public String getUsername(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                    .get("username", String.class);
        } catch (ExpiredJwtException e) {
    
            return e.getClaims().getSubject(); // <-- 이 부분 수정
        } catch (Exception e) {
            // 다른 예외 (예: SignatureException, MalformedJwtException 등) 처리
            // 토큰이 유효하지 않은 경우 (변조 등) null을 반환하거나 특정 예외를 던질 수 있습니다.
            // 현재 로직에서는 NullPointerException을 방지하기 위해 null 반환
            return null;
        }
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration()
                    .before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 토큰이 만료되었을 경우 발생하는 예외 처리
            return true;
        } catch (Exception e) {
            // 다른 예외 발생 시 (예: 토큰 변조), 유효하지 않다고 간주
            return true;
        }
    }

    /**
     * 토큰에서 JTI(JWT ID) 추출
     */
    public String getJti(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getId();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 토큰이 만료되었을 경우, ExpiredJwtException 객체에서 클레임을 직접 가져와 JTI 추출
            // 만료되었더라도 서명이 유효하면 페이로드(클레임)는 읽을 수 있어야 합니다.
            return e.getClaims().getId();
        } catch (Exception e) {
            // 다른 예외 발생 시 (예: 토큰 변조 등)
            System.err.println("Failed to get JTI from token: " + e.getMessage()); // 로그 출력
            throw new IllegalArgumentException("Invalid token or missing JTI", e); // 예외 던지기
        }
    }

    /**
     * 토큰에서 만료 시간 추출 (LocalDateTime 반환)
     */
    public LocalDateTime getExpirationLocalDateTime(String token) {
        try {
            Date expirationDate = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                    .getExpiration();
            return expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return null; // 토큰이 유효하지 않거나 만료 시간 없음
        }
    }

    /**
     * 토큰 발급 : accessToken
     */
    public String createAccessToken(String username, String role) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .signWith(secretKey)
                .id(jti)
                .compact();
    }

    /**
     * 토큰 발급 : refreshToken
     */
    public String createRefreshToken(String username) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(secretKey)
                .id(jti)
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

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);    //  "   Bearer_" 이후의 토큰 부분
            //    0123456
        }
        throw new RuntimeException("Authorization header is missing or invalid format");
    }
}
