package com.sejong.userservice.support.common.security.oauth;

import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import com.sejong.userservice.support.common.security.oauth.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    @Value("${app.oauth2.success-redirect-uri}")
    private String successRedirectUri;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // Access Token 생성
        String accessToken = jwtUtil.createAccessToken(username, role);
        
        // Refresh Token 생성
        String refreshToken = jwtUtil.createRefreshToken(username);

        // Access Token을 쿠키에 저장 (API Gateway에서 읽기 위해)
        Cookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);
        
        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        // 프론트 앱 리다이렉트
        response.sendRedirect(successRedirectUri);
    }
}

