package com.sejong.userservice.application.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sejong.userservice.application.chat.dto.BroadCastDto;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.publisher.ChatEventPublisher;
import com.sejong.userservice.application.chat.publisher.RedisPublisher;
import com.sejong.userservice.application.chat.service.ChatHandleMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisPublisher redisPublisher;
    private final ChatEventPublisher chatEventPublisher;
    private final ChatHandleMessageService chatHandleMessageService;

    //세션 연결되면 바로 실행되는 callback method
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = getCurrentUser(session);
        log.info("username : {}님이 세션에 연결되었습니다.", username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = chatHandleMessageService.getPayLoad(message);
        ChatMessageDto msg = ChatMessageDto.from(payload);

        switch (msg.getType().toUpperCase()) {
            case "CREATED" -> handleCreate(session);
            case "JOIN" -> handleJoin(msg, session);
            case "CHAT" -> handleChat(msg);
            case "CLOSE" -> handleClose(msg, session);
            default -> log.info("error Type");
        }
    }

    private void handleCreate(WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleCreate(session);
        redisPublisher.publish(broadCastDto.getChatMessageDto());
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }

    private void handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleJoin(chatMessageDto, session);
        redisPublisher.publish(broadCastDto.getChatMessageDto());
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }

    private void handleChat(ChatMessageDto chatMessageDto) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleChat(chatMessageDto);
        redisPublisher.publish(broadCastDto.getChatMessageDto());
        chatEventPublisher.publish(broadCastDto.getChatMessageDto());
    }


    private void handleClose(ChatMessageDto chatMessageDto, WebSocketSession session) {
        BroadCastDto broadCastDto = chatHandleMessageService.handleClose(chatMessageDto, session);
        redisPublisher.publish(broadCastDto.getChatMessageDto());
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
