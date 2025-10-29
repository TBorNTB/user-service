package com.sejong.userservice.application.chat.interceptor;

import com.sejong.userservice.application.common.security.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;

    public CustomWebSocketInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = resolveToken(httpRequest);
            jwtUtil.validateToken(token); //null이 아닌걸 내부 메서드에서 확정
            String username = jwtUtil.getUsername(token);
            String userRole = jwtUtil.getRole(token);
            attributes.put("username", username);
            attributes.put("userRole", userRole);
            return true;
        }
        return false;
    }

    private String resolveToken(HttpServletRequest httpRequest) {
        String bearerToken = httpRequest.getHeader("Authorization");
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
