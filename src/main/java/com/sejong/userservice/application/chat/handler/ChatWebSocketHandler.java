package com.sejong.userservice.application.chat.handler;

import com.sejong.userservice.application.chat.dto.BroadCastDto;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.publisher.ChatEventPublisher;
import com.sejong.userservice.application.chat.service.ChatHandleMessageService;
import com.sejong.userservice.application.user.UserService;
import com.sejong.userservice.core.user.User;
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

    private final ChatEventPublisher chatEventPublisher;
    private final ChatHandleMessageService chatHandleMessageService;
    private final UserService userService;
    //세션 연결되면 바로 실행되는 callback method
    //즉 최초 한번만 실행됨.. DB 조회 후 session 에 저장
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        String username = getCurrentUser(session);
        User user = userService.getUser(username);
        session.getAttributes().put("nickname", user.getNickname());
        session.getAttributes().put("username",username);
        log.info("username : {}님이 세션에 연결되었습니다.", username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = chatHandleMessageService.getPayLoad(message);
        String username = session.getAttributes().get("username").toString();
        String nickname = session.getAttributes().get("nickname").toString();
        ChatMessageDto msg = ChatMessageDto.from(payload, username, nickname);

        switch (msg.getType().toUpperCase()) {
            case "JOIN" -> handleJoin(msg, session);
            case "CHAT" -> handleChat(msg, session);
            case "CLOSE" -> handleClose(msg, session);
            default -> log.info("error Type");
        }
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

    //이거는 나가기 버튼누른게 아니라 사용자가 강제로 브라우저를 종료할 때 실행되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        chatHandleMessageService.leaveRooms(session);
        String username = getCurrentUser(session);
        log.info("username : {} 사용자가 map 세션에서 제외 되었습니다.", username);
    }

    private String getCurrentUser(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

}
