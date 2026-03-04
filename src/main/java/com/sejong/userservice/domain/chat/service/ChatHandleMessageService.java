package com.sejong.userservice.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.domain.chat.dto.BroadCastDto;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.support.common.sanitizer.RequestSanitizer;
import com.sejong.userservice.support.config.ServerIdProvider;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandleMessageService {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ServerIdProvider serverIdProvider;
    private final RequestSanitizer requestSanitizer;

    public Map<String, Object> getPayLoad(TextMessage message) throws JsonProcessingException {
        return objectMapper.readValue(message.getPayload(), Map.class);
    }

    public BroadCastDto handleClose(ChatMessageDto msg, WebSocketSession session) {
        msg = requestSanitizer.sanitize(msg);
        validateSessionInRoom(msg.getRoomId(), session);
        String roomId = msg.getRoomId();
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
                log.info("빈방 삭제 : {}", roomId);
            }
        }
        return BroadCastDto.of(ChatMessageDto.chatClose(msg, serverIdProvider.getServerId()));
    }

    //해당 세션이 실제로 ROOMiD에 존재하는지 안한다면 따로 처리를 해줘야 될듯??
    public BroadCastDto handleChat(ChatMessageDto msg, WebSocketSession session) {
        msg = requestSanitizer.sanitize(msg);
        validateSessionInRoom(msg.getRoomId(), session);
        ChatMessageDto messageDto = ChatMessageDto.chatMethod(msg, serverIdProvider.getServerId());
        return BroadCastDto.of(messageDto);
    }

    /**
     * Kafka Consumer에서 호출. 해당 방의 로컬 WebSocket 세션들에게만 전달.
     */
    public void broadcast(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }

    public BroadCastDto handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        chatMessageDto = requestSanitizer.sanitize(chatMessageDto);
        String roomId = chatMessageDto.getRoomId();
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        ChatMessageDto responseMessage = ChatMessageDto.joinMethod(chatMessageDto, serverIdProvider.getServerId());
        return BroadCastDto.of(responseMessage);
    }

    /**
     * 세션이 속한 모든 방에서 제거.
     */
    public void leaveRooms(WebSocketSession session) {
        roomSessions.forEach((roomId, sessions) -> {
            if (sessions == null) return;
            synchronized (sessions) {
                if (sessions.remove(session)) {
                    if (sessions.isEmpty()) {
                        roomSessions.remove(roomId);
                        log.info("빈 방 삭제: {}", roomId);
                    }
                }
            }
        });
    }

    private void validateSessionInRoom(String roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if(sessions == null || !sessions.contains(session))throw new RuntimeException("해당 roomId에 세션이 존재하지 않는다.");
    }
}
