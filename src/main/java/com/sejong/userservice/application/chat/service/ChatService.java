package com.sejong.userservice.application.chat.service;

import com.sejong.userservice.application.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.application.chat.dto.ChatMessageDto;
import com.sejong.userservice.application.chat.dto.ChatMessageEvent;
import com.sejong.userservice.core.chat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;


    @Transactional
    public void save(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = ChatMessage.from(chatMessageDto);
        chatRepository.save(chatMessage);
    }

    @Transactional
    public SingleRoomResponse createRoom(String newRoomId, String friendUsername, String myUsername) {
        ChatRoom chatRoom = ChatRoom.createSingle(newRoomId);
        List<ChatUser> chatUsers = ChatUser.makeChatUsers(List.of(friendUsername,myUsername), newRoomId);
        ChatRoom savedRoom = chatRepository.createRoom(chatRoom, chatUsers);
        return SingleRoomResponse.of(savedRoom.getRoomId(),savedRoom.getRoomName());
    }
}
