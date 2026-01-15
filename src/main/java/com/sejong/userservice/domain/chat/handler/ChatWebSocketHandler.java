package com.sejong.userservice.domain.chat.handler;

import com.sejong.userservice.domain.chat.dto.BroadCastDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.publisher.ChatEventPublisher;
import com.sejong.userservice.domain.chat.service.ChatHandleMessageService;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.service.UserService;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String AUTHENTICATED_KEY = "authenticated";
    private static final String USERNAME_KEY = "username";
    private static final String NICKNAME_KEY = "nickname";

    private final ChatEventPublisher chatEventPublisher;
    private final ChatHandleMessageService chatHandleMessageService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    /**
     * WebSocket 연결이 성공적으로 설정되었을 때 호출
     * 인증은 첫 메시지(AUTH)에서 처리하므로 여기서는 사용자 정보를 조회하지 않음
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 세션이 연결되었습니다. 인증을 기다립니다. sessionId: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = chatHandleMessageService.getPayLoad(message);
        String messageType = ((String) payload.get("type")).toUpperCase();

        // 첫 메시지는 반드시 AUTH 타입이어야 함
        if (!isAuthenticated(session)) {
            if ("AUTH".equals(messageType)) {
                handleAuth(payload, session);
                return;
            } else {
                log.warn("인증되지 않은 세션에서 AUTH가 아닌 메시지 수신. 연결을 종료합니다. sessionId: {}, type: {}", 
                        session.getId(), messageType);
                session.close(CloseStatus.POLICY_VIOLATION.withReason("인증되지 않은 세션입니다. 먼저 AUTH 메시지를 보내주세요."));
                return;
            }
        }

        // 인증된 세션에서만 다른 메시지 타입 처리
        String username = getCurrentUser(session);
        String nickname = getNickname(session);
        ChatMessageDto msg = ChatMessageDto.from(payload, username, nickname);

        switch (messageType) {
            case "JOIN" -> handleJoin(msg, session);
            case "CHAT" -> handleChat(msg, session);
            case "CLOSE" -> handleClose(msg, session);
            default -> log.warn("알 수 없는 메시지 타입: {}", messageType);
        }
    }

    /**
     * AUTH 메시지 처리: 토큰 검증 및 사용자 정보 세션에 저장
     */
    private void handleAuth(Map<String, Object> payload, WebSocketSession session) {
        try {
            String token = (String) payload.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("AUTH 메시지에 토큰이 없습니다. sessionId: {}", session.getId());
                session.close(CloseStatus.POLICY_VIOLATION.withReason("토큰이 필요합니다."));
                return;
            }

            // 토큰 검증
            jwtUtil.validateToken(token);
            String username = jwtUtil.getUsername(token);
            
            if (username == null) {
                log.warn("토큰에서 username을 추출할 수 없습니다. sessionId: {}", session.getId());
                session.close(CloseStatus.POLICY_VIOLATION.withReason("유효하지 않은 토큰입니다."));
                return;
            }

            // 사용자 정보 조회 및 세션에 저장
            User user = userService.getUser(username);
            session.getAttributes().put(AUTHENTICATED_KEY, true);
            session.getAttributes().put(USERNAME_KEY, username);
            session.getAttributes().put(NICKNAME_KEY, user.getNickname());

            log.info("인증 성공 - username: {}, nickname: {}, sessionId: {}", 
                    username, user.getNickname(), session.getId());
        } catch (Exception e) {
            log.error("AUTH 처리 중 오류 발생. sessionId: {}", session.getId(), e);
            try {
                session.close(CloseStatus.SERVER_ERROR.withReason("인증 처리 중 오류가 발생했습니다: " + e.getMessage()));
            } catch (Exception closeException) {
                log.error("세션 종료 중 오류 발생", closeException);
            }
        }
    }

    /**
     * 세션이 인증되었는지 확인
     */
    private boolean isAuthenticated(WebSocketSession session) {
        Boolean authenticated = (Boolean) session.getAttributes().get(AUTHENTICATED_KEY);
        return authenticated != null && authenticated;
    }

    /**
     * 세션에서 현재 사용자명 가져오기
     */
    private String getCurrentUser(WebSocketSession session) {
        return (String) session.getAttributes().get(USERNAME_KEY);
    }

    /**
     * 세션에서 닉네임 가져오기
     */
    private String getNickname(WebSocketSession session) {
        return (String) session.getAttributes().get(NICKNAME_KEY);
    }

    private void handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleJoin(chatMessageDto, session);
        // 로컬 세션에 즉시 브로드캐스트 (지연 최소화)
        chatHandleMessageService.broadcastToLocalSessions(broadCastDto.getChatMessageDto());
        // Kafka에 발행 (영구 저장 + 다른 서버 전파용)
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }

    private void handleChat(ChatMessageDto chatMessageDto, WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleChat(chatMessageDto,session);
        // 로컬 세션에 즉시 브로드캐스트 (지연 최소화)
        chatHandleMessageService.broadcastToLocalSessions(broadCastDto.getChatMessageDto());
        // Kafka에 발행 (영구 저장 + 다른 서버 전파용)
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }

    private void handleClose(ChatMessageDto chatMessageDto, WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleClose(chatMessageDto, session);
        // 로컬 세션에 즉시 브로드캐스트 (지연 최소화)
        chatHandleMessageService.broadcastToLocalSessions(broadCastDto.getChatMessageDto());
        // Kafka에 발행 (영구 저장 + 다른 서버 전파용)
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }

    /**
     * 사용자가 강제로 브라우저를 종료하거나 연결이 끊어졌을 때 실행되는 메서드
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        chatHandleMessageService.leaveRooms(session);
        String username = getCurrentUser(session);
        if (username != null) {
            log.info("username: {} 사용자가 세션에서 제외되었습니다. sessionId: {}", username, session.getId());
        } else {
            log.info("인증되지 않은 세션이 종료되었습니다. sessionId: {}", session.getId());
        }
    }

}
