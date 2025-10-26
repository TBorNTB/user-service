package com.sejong.userservice.application.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.enums.ChatType;
import com.sejong.userservice.application.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //세션 연결되면 바로 실행되는 callback method
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = getCurrentUser(session);
        log.info("username : {}님이 세션에 연결되었습니다.",username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String,Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

        ChatMessageDto msg = ChatMessageDto.from(payload);

        switch (msg.getType().toUpperCase()) {
            case "CREATED" -> handleCreate(session);
            case "JOIN" -> handleJoin(msg, session);
            case "CHAT" -> handleChat(msg);
            case "CLOSE" -> handleClose(msg,session);
            default -> log.info("error Type");
        }
    }

    private void handleClose(ChatMessageDto msg,WebSocketSession session) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(msg.getRoomId());
        if (sessions != null) {
            sessions.remove(session);
            String json = "퇴장";
            broadcast(msg.getRoomId(),json);
            if(sessions.isEmpty()){
                roomSessions.remove(msg.getRoomId());
                log.info("빈방 삭제 : {}",msg.getRoomId());
            }
        }
    }

    //해당 세션이 실제로 ROOMiD에 존재하는지 안한다면 따로 처리를 해줘야 될듯??
    private void handleChat(ChatMessageDto msg) throws IOException {
        ChatMessageDto messageDto = ChatMessageDto.chatMethod(msg);
        String json = objectMapper.writeValueAsString(messageDto);
        broadcast(msg.getRoomId(), json);
    }

    private void broadcast(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        for(WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(json));
        }
    }

    private void handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) throws IOException {
        String roomId = chatMessageDto.getRoomId();
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        sessions.add(session);

        ChatMessageDto responseMessage = ChatMessageDto.joinMethod(chatMessageDto);
        String json = objectMapper.writeValueAsString(responseMessage);
        broadcast(chatMessageDto.getRoomId(), json);
    }

    private void handleCreate(WebSocketSession session) throws Exception {
        String newRoomId = UUID.randomUUID().toString();
        roomSessions.put(newRoomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(newRoomId).add(session);

        ChatMessageDto msg = ChatMessageDto.createMethod(newRoomId);
        String json = objectMapper.writeValueAsString(msg);
        broadcast(newRoomId, json);
        log.info("방생성 완료");
    }

    //이거는 나가기 버튼누른게 아니라 사용자가 강제로 브라우저를 종료할 때 실행되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        leaveRooms(session);
        String username = getCurrentUser(session);
        log.info("username : {} 사용자가 map 세션에서 제외 되었습니다.",username);
    }

    private void leaveRooms(WebSocketSession session) {
        roomSessions.forEach((roomId,sessions)->{
            if(sessions.remove(session)&&sessions.isEmpty()){
                roomSessions.remove(roomId);
            }
        });
    }

    private String getCurrentUser(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

}
