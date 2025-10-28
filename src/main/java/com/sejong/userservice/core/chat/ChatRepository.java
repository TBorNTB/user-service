package com.sejong.userservice.core.chat;

import java.util.List;

public interface ChatRepository {
    void save(ChatMessage chatMessage);
    ChatRoom createRoom(ChatRoom chatRoom, List<ChatUser> chatUsers);
}
