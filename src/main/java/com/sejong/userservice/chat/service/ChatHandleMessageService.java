package com.sejong.userservice.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.chat.dto.BroadCastDto;
import com.sejong.userservice.chat.dto.ChatMessageDto;
import com.sejong.userservice.config.ServerIdProvider;
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
        return BroadCastDto.of(ChatMessageDto.chatClose(msg, serverIdProvider.getServerId()));
    }

    //해당 세션이 실제로 ROOMiD에 존재하는지 안한다면 따로 처리를 해줘야 될듯??
    public BroadCastDto handleChat(ChatMessageDto msg, WebSocketSession session) {
        validateSessionInRoom(msg.getRoomId(), session);
        ChatMessageDto messageDto = ChatMessageDto.chatMethod(msg, serverIdProvider.getServerId());
        return BroadCastDto.of(messageDto);
    }

    public void broadcast(String roomId, String json) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        for (WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * 로컬 서버의 세션들에게만 즉시 브로드캐스트 (지연 최소화)
     * serverId를 메시지에 추가하여 Redis를 통한 중복 전송 방지
     */
    public void broadcastToLocalSessions(ChatMessageDto chatMessageDto) {
        String roomId = chatMessageDto.getRoomId();
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("로컬 브로드캐스트 스킵 - roomId: {}에 세션 없음", roomId);
            return;
        }

        try {
            // 서버 ID 추가 (중복 방지용)
            chatMessageDto.setServerId(serverIdProvider.getServerId());

            String json = objectMapper.writeValueAsString(chatMessageDto);
            int sentCount = 0;
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                    sentCount++;
                }
            }
            log.info("로컬 브로드캐스트 완료 - roomId: {}, serverId: {}, 전송 세션 수: {}/{}",
                    roomId, serverIdProvider.getServerId(), sentCount, sessions.size());
        } catch (Exception e) {
            log.error("로컬 브로드캐스트 실패 - roomId: {}", roomId, e);
        }
    }

    public BroadCastDto handleJoin(ChatMessageDto chatMessageDto, WebSocketSession session) {
        String roomId = chatMessageDto.getRoomId();
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        ChatMessageDto responseMessage = ChatMessageDto.joinMethod(chatMessageDto, serverIdProvider.getServerId());
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
