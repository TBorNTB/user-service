package com.sejong.userservice.infrastructure.chat;

import com.sejong.userservice.core.chat.ChatMessage;
import com.sejong.userservice.core.chat.ChatRepository;
import com.sejong.userservice.core.chat.ChatRoom;
import com.sejong.userservice.core.chat.ChatUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final JpaChatRepository jpaChatRepository;
    private final JpaChatRoomRepository jpaChatRoomRepository;
    private final JpaChatUserRepository jpaChatUserRepository;

    @Override
    public void save(ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.from(chatMessage);
        jpaChatRepository.save(entity);
        log.info("db 저장 성공~");
    }

    @Override
    public ChatRoom createRoom(ChatRoom chatRoom, List<ChatUser> chatUsers) {
        ChatRoomEntity chatRoomEntity = ChatRoomEntity.from(chatRoom);
        List<ChatUserEntity> chatUserEntities = chatUsers.stream()
                .map(ChatUserEntity::from)
                .toList();

        ChatRoomEntity savedChatRoomEntity = jpaChatRoomRepository.save(chatRoomEntity);
        jpaChatUserRepository.saveAll(chatUserEntities);
        return savedChatRoomEntity.toDomain();
    }

    @Override
    public ChatRoom findRoomById(String roomId) {
        ChatRoomEntity chatRoomEntity = jpaChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("해당 roomId는 존재하지 않습니다."));
        return chatRoomEntity.toDomain();
    }

    @Override
    public ChatRoom addRoomMembers(ChatRoom chatRoom, List<ChatUser> chatUsers) {
        ChatRoomEntity chatRoomEntity = jpaChatRoomRepository.findById(chatRoom.getRoomId())
                .orElseThrow(() -> new RuntimeException("해당 roomId는 존재하지 않습니다."));
        chatRoomEntity.updateRoomName(chatRoom.getRoomName()); //변경 감지 때문에 save따로 호출 x

        List<ChatUserEntity> chatUserEntities = chatUsers.stream()
                .map(ChatUserEntity::from)
                .toList();

        jpaChatUserRepository.saveAll(chatUserEntities);
        return chatRoomEntity.toDomain();
    }

    @Override
    public String removeUsernameInRoom(String roomId, String username) {
        boolean exists = jpaChatRoomRepository.existsById(roomId);
        if (!exists) {
            throw new RuntimeException("해당 roomId의 방은 존재하지 않습니다.");
        }
        jpaChatUserRepository.deleteByUsername(username, roomId);// 쓰기지연
        Integer remainPersonCount = jpaChatUserRepository.getCountUserByUsername(roomId); // 읽기쿼리 발생했기 때문에 삭제 반영 된 뒤 읽게됨
        if (remainPersonCount == 0) {
            jpaChatRoomRepository.deleteById(roomId);
        }

        return roomId;
        //내가 마지막 인원이였으면 =? 해당 채팅방 자체를 삭제해야 되잖아
    }

    @Override
    public List<ChatRoom> findAllRooms(String username) {
        List<ChatUserEntity> chatUserEntities = jpaChatUserRepository.findAllByUsername(username);
        List<ChatRoomEntity> chatRoomEntities = chatUserEntities.stream()
                .map(it -> {
                    return jpaChatRoomRepository.findById(it.getRoomId())
                            .orElseThrow(() -> new RuntimeException("해당 roomId는 존재하지 않습니다."));
                }).toList();

        return chatRoomEntities.stream()
                .map(ChatRoomEntity::toDomain)
                .toList();
    }

    @Override
    public List<ChatMessage> findAllChatMessages(String roomId) {
        List<ChatMessageEntity> chatMessageEntities = jpaChatRepository.findAllChatMessages(roomId);
        return chatMessageEntities.stream()
                .map(ChatMessageEntity::toDomain)
                .toList();
    }

}
