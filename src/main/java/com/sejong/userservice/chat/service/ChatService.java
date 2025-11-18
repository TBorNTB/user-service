package com.sejong.userservice.chat.service;

import static com.sejong.userservice.application.common.exception.ExceptionType.DM_ROOM_WITH_OTHER_PERSON;
import static com.sejong.userservice.application.common.exception.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.application.common.exception.ExceptionType.ROOM_ID_NOT_FOUND;

import com.sejong.userservice.application.common.exception.BaseException;
import com.sejong.userservice.chat.constant.ChatRoomUserRole;
import com.sejong.userservice.chat.controller.response.ChatMessageResponse;
import com.sejong.userservice.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.chat.controller.response.RoomResponse;
import com.sejong.userservice.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.chat.domain.ChatMessage;
import com.sejong.userservice.chat.domain.ChatRoom;
import com.sejong.userservice.chat.domain.ChatRoomUser;
import com.sejong.userservice.chat.dto.ChatMessageDto;
import com.sejong.userservice.chat.repository.ChatMessageRepository;
import com.sejong.userservice.chat.repository.ChatRoomRepository;
import com.sejong.userservice.chat.repository.ChatRoomUserRepository;
import com.sejong.userservice.infrastructure.user.JpaUserRepository;
import com.sejong.userservice.infrastructure.user.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JpaUserRepository jpaUserRepository; // TODO(sigmaith): refactoring 필요
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;


    @Transactional
    public void save(ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));
        ChatMessage chatMessage = ChatMessage.from(chatMessageDto, chatRoom);
        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public SingleRoomResponse createDMRoom(String newRoomId, String friendUsername, String myUsername) {
        UserEntity friend = jpaUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        UserEntity owner = jpaUserRepository.findByUsername(myUsername)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        if (friend.equals(owner)) throw new BaseException(DM_ROOM_WITH_OTHER_PERSON);

        // 1. ChatRoom 생성 및 저장
        ChatRoom chatRoom = ChatRoom.dmRoom(newRoomId);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 2. 단방향 관계: ChatRoomUser를 직접 생성하여 저장
        ChatRoomUser ownerJoin = ChatRoomUser.join(savedRoom, owner, ChatRoomUserRole.OWNER);
        chatRoomUserRepository.save(ownerJoin);

        ChatRoomUser memberJoin = ChatRoomUser.join(savedRoom, friend, ChatRoomUserRole.MEMBER);
        chatRoomUserRepository.save(memberJoin);

        return SingleRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse createGroupRoom(String newRoomId, String roomName, List<String> friendsUsername,
                                             String myUsername) {
        // 1. ChatRoom 생성 및 저장
        ChatRoom chatRoom = ChatRoom.groupRoom(newRoomId, roomName);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 2. 단방향 관계: 방장(myUsername)을 OWNER로 추가
        UserEntity owner = jpaUserRepository.findByUsername(myUsername)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        ChatRoomUser ownerJoin = ChatRoomUser.join(savedRoom, owner, ChatRoomUserRole.OWNER);
        chatRoomUserRepository.save(ownerJoin);

        // 3. 나머지 멤버들을 MEMBER로 추가
        List<UserEntity> friends = jpaUserRepository.findByUsernameIn(friendsUsername);
        for (UserEntity friend : friends) {
            ChatRoomUser memberJoin = ChatRoomUser.join(savedRoom, friend, ChatRoomUserRole.MEMBER);
            chatRoomUserRepository.save(memberJoin);
        }

        return GroupRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse addRoomMembers(String roomId, List<String> friendsUsername) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        // 단방향 관계: 새 멤버들을 ChatRoomUser로 직접 추가
        List<UserEntity> users = jpaUserRepository.findByUsernameIn(friendsUsername);
        for (UserEntity user : users) {
            if (!chatRoomUserRepository.existsByUsernameAndRoomId(user.getUsername(), chatRoom.getRoomId())) {
                ChatRoomUser memberJoin = ChatRoomUser.join(chatRoom, user, ChatRoomUserRole.MEMBER);
                chatRoomUserRepository.save(memberJoin);
            }
        }

        return GroupRoomResponse.of(chatRoom.getRoomId(), chatRoom.getRoomName());
    }

    @Transactional
    public RoomResponse quitRoom(String roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        UserEntity user = jpaUserRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        chatRoomUserRepository.deleteByUsernameAndRoomId(user.getUsername(), chatRoom.getRoomId());
        if (chatRoomUserRepository.getCountByRoomId(chatRoom.getRoomId()) == 0) {
            chatRoomRepository.deleteById(roomId);
        }
        return RoomResponse.quitOf(roomId);
    }

    public List<GroupRoomResponse> findAllRooms(String username) {
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByUsername(username);
        return chatRoomUsers.stream()
                .map(cru -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(cru.getChatRoom().getRoomId())
                            .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));
                    return GroupRoomResponse.of(chatRoom.getRoomId(), chatRoom.getRoomName());
                })
                .toList();
    }

    public List<ChatMessageResponse> findAllChatMessages(String roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllChatMessages(roomId);
        return chatMessages.stream()
                .map(it -> {
                    UserEntity user = jpaUserRepository.findByUsername(it.getUsername())
                            .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
                    return ChatMessageResponse.of(it, user.getNickname());
                }).toList();
    }
}
