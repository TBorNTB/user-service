package com.sejong.userservice.application.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.application.chat.dto.BroadCastDto;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
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

    public Map<String, Object> getPayLoad(TextMessage message) throws JsonProcessingException {
        return objectMapper.readValue(message.getPayload(), Map.class);
    }

    public BroadCastDto handleClose(ChatMessageDto msg, WebSocketSession session) {
        validateSessionInRoom(msg.getRoomId(), session);
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
    public BroadCastDto handleChat(ChatMessageDto msg, WebSocketSession session) {
        validateSessionInRoom(msg.getRoomId(), session);
        ChatMessageDto messageDto = ChatMessageDto.chatMethod(msg);
        return BroadCastDto.of(messageDto);
    }

    public void broadcast(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(json));
        }
    }

    public BroadCastDto handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        String roomId = chatMessageDto.getRoomId();
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        ChatMessageDto responseMessage = ChatMessageDto.joinMethod(chatMessageDto);
        return BroadCastDto.of(responseMessage);
    }

    public void leaveRooms(WebSocketSession session) {
        roomSessions.entrySet().removeIf(entry -> {
            Set<WebSocketSession> sessions = entry.getValue();
            if (sessions == null) return false;
            synchronized (sessions) {
                // NOTE(sigmaith): 같은 roomId에 여러 스레드가 접근할 수 있다.
                // 빈방 삭제와 거의 동시에 session 추가 메서드가 실행되면, 없어진 참조를 가질 수 있다.
                boolean removed = sessions.remove(session);
                boolean empty = sessions.isEmpty();

                if (removed && empty) {
                    log.info("빈 방 삭제: {}", entry.getKey());
                    return true;
                }
            }
            return false;
        });
    }

    private void validateSessionInRoom(String roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if(sessions == null || !sessions.contains(session))throw new RuntimeException("해당 roomId에 세션이 존재하지 않는다.");
    }
}
