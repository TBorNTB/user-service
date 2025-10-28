package com.sejong.userservice.application.chat.service;

import com.sejong.userservice.application.chat.controller.response.ChatMessageResponse;
import com.sejong.userservice.application.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.application.chat.controller.response.RoomResponse;
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
        List<ChatUser> chatUsers = ChatUser.makeChatUsers(List.of(friendUsername, myUsername), newRoomId);
        ChatRoom savedRoom = chatRepository.createRoom(chatRoom, chatUsers);
        return SingleRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse createRooms(String newRoomId, String roomName, List<String> friendsUsername, String myUsername) {
        ChatRoom chatRoom = ChatRoom.createGroup(newRoomId, roomName);
        friendsUsername.add(myUsername); //편의상 List에 넣어서 전송
        List<ChatUser> chatUsers = ChatUser.makeChatUsers(friendsUsername, chatRoom.getRoomId());
        ChatRoom savedRoom = chatRepository.createRoom(chatRoom, chatUsers);
        return GroupRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse addRoomMembers(String roomId, String roomName, List<String> friendsUsername) {
        ChatRoom chatRoom = chatRepository.findRoomById(roomId);
        chatRoom.updateRoomName(roomName);
        List<ChatUser> chatUsers = ChatUser.makeChatUsers(friendsUsername, roomId);
        ChatRoom savedRoom = chatRepository.addRoomMembers(chatRoom, chatUsers);
        return GroupRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public RoomResponse quitRoom(String roomId, String username) {
        String responseRoomId = chatRepository.removeUsernameInRoom(roomId, username);
        return RoomResponse.deleteOf(responseRoomId);
    }

    public List<GroupRoomResponse> findAllRooms(String username) {
        List<ChatRoom> chatRooms = chatRepository.findAllRooms(username);
        return chatRooms.stream()
                .map(it-> GroupRoomResponse.of(it.getRoomId(), it.getRoomName()))
                .toList();
    }

    public List<ChatMessageResponse> findAllChatMessages(String roomId) {
        List<ChatMessage> chatMessages = chatRepository.findAllChatMessages(roomId);
        return chatMessages.stream()
                .map(it->{
                    return ChatMessageResponse.of(it,"임시닉네임입니다 변경필요");
                }).toList();
    }
}
