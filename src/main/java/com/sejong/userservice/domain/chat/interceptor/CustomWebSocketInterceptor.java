package com.sejong.userservice.domain.chat.interceptor;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket Handshake Interceptor
 * 쿠키/헤더 기반 인증을 제거하고, 첫 메시지(AUTH)로 인증을 처리하도록 변경됨
 */
@Component
@Slf4j
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        log.info("WebSocket handshake 시작 - 인증은 첫 메시지(AUTH)로 처리됩니다.");
        // 인증 없이 연결 허용 (인증은 첫 메시지에서 처리)
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        log.info("WebSocket handshake 완료");
    }
}
