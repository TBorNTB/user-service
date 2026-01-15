package com.sejong.userservice.domain.chat.interceptor;

import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@Slf4j
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JWTUtil jwtUtil;

    public CustomWebSocketInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        log.info("before handshake start");

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();

        String token = resolveTokenFromCookie(httpRequest);
        if (token == null) {
            token = resolveTokenFromAuthorizationHeader(httpRequest); // (선택) 기존 방식 fallback
        }

        if (token == null) {
            return false; // 토큰 없으면 연결 거부
        }

        jwtUtil.validateToken(token);

        String username = jwtUtil.getUsername(token);
        String userRole = jwtUtil.getRole(token);

        attributes.put("username", username);
        attributes.put("userRole", userRole);

        return true;
    }

    private String resolveTokenFromCookie(HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveTokenFromAuthorizationHeader(HttpServletRequest httpRequest) {
        String bearerToken = httpRequest.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) { }
}
