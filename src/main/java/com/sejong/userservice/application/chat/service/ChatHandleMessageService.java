package com.sejong.userservice.application.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.BroadCastDto;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandleMessageService {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public Map<String, Object> getPayLoad(TextMessage message) throws JsonProcessingException {
        return objectMapper.readValue(message.getPayload(), Map.class);
    }

    public BroadCastDto handleClose(ChatMessageDto msg, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(msg.getRoomId());
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(msg.getRoomId());
                log.info("빈방 삭제 : {}", msg.getRoomId());
            }
        }
        return BroadCastDto.of(ChatMessageDto.chatClose(msg));
    }

    //해당 세션이 실제로 ROOMiD에 존재하는지 안한다면 따로 처리를 해줘야 될듯??
    public BroadCastDto handleChat(ChatMessageDto msg) {
        ChatMessageDto messageDto = ChatMessageDto.chatMethod(msg);
        return BroadCastDto.of(messageDto);
    }

    public void broadcast(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(json));
        }
    }

    public BroadCastDto handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        String roomId = chatMessageDto.getRoomId();
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        sessions.add(session);

        ChatMessageDto responseMessage = ChatMessageDto.joinMethod(chatMessageDto);
        return BroadCastDto.of(responseMessage);
    }

    public BroadCastDto handleCreate(WebSocketSession session) {
        String newRoomId = UUID.randomUUID().toString();
        roomSessions.put(newRoomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(newRoomId).add(session);

        ChatMessageDto msg = ChatMessageDto.createMethod(newRoomId);
        log.info("방생성 완료");
        return BroadCastDto.of(msg);
    }

    public void leaveRooms(WebSocketSession session) {
        roomSessions.forEach((roomId, sessions) -> {
            if (sessions.remove(session) && sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        });
    }
}
