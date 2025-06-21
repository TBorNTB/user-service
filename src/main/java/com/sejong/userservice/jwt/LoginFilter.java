package com.sejong.userservice.jwt;

import com.sejong.userservice.api.controller.dto.CustomUserDetails;
import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        // request 값 검증을 위해 DTO로 변환
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();


        String accessToken = jwtUtil.createAccessToken(username, role);
        String refreshToken = jwtUtil.createRefreshToken(username);

        // Refresh Token 정보 추출
        String jti = jwtUtil.getJti(refreshToken);
        LocalDateTime expiryDate = jwtUtil.getExpirationLocalDateTime(refreshToken);

        // 이전 Refresh Token 무효화 및 새 Refresh Token 저장 (RTR 핵심)
        // 이 사용자의 모든 기존 토큰을 무효화/삭제 (새로운 로그인 시)
        refreshTokenRepository.revokeAllTokensForUser(username); // 이 메서드를 통해 기존 토큰 무효화
        refreshTokenRepository.saveRefreshToken(refreshToken, username, expiryDate, jti); // 새로운 토큰 저장


        response.addHeader("Authorization", "Bearer " + accessToken);

        Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}
