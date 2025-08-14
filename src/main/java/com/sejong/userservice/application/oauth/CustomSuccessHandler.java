package com.sejong.userservice.application.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.jwt.dto.CustomOAuth2User;
import com.sejong.userservice.infrastructure.common.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;


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

        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        // Access Token을 JSON 응답으로 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", accessToken);
        responseData.put("username", username);
        responseData.put("role", role);
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(responseData));
    }
}

