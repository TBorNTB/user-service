package com.sejong.userservice.infrastructure.common.jwt;

import com.sejong.userservice.application.user.dto.CustomUserDetails;
import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.token.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 로그인 필터처리 시 재이용할 것.
@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepository;

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        // 기본적으로 'username'을 사용하던 것을 'email'로 변경
        return request.getParameter("email");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password,
                null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getEmail();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String accessToken = jwtUtil.createAccessToken(username, role);
        String refreshToken = jwtUtil.createRefreshToken(username);

        // Token 정보 추출
        String accessJti = jwtUtil.getJti(accessToken);
        LocalDateTime accessExpiryDate = jwtUtil.getExpirationLocalDateTime(accessToken);
        String refreshJti = jwtUtil.getJti(refreshToken);
        LocalDateTime refreshExpiryDate = jwtUtil.getExpirationLocalDateTime(refreshToken);

        tokenRepository.revokeAllTokensForUser(username);
        tokenRepository.saveToken(accessToken, username, accessExpiryDate, accessJti, TokenType.ACCESS);
        tokenRepository.saveToken(refreshToken, username, refreshExpiryDate, refreshJti, TokenType.REFRESH);

        response.addHeader("Authorization", "Bearer " + accessToken);

        Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        response.setStatus(401);
    }
}
