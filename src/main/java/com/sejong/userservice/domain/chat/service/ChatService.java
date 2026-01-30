package com.sejong.userservice.domain.chat.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.DM_ROOM_WITH_OTHER_PERSON;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.ROOM_ID_NOT_FOUND;

import com.sejong.userservice.domain.chat.constant.ChatRoomUserRole;
import com.sejong.userservice.domain.chat.controller.response.ChatMessageResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatMessagesPageResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatRoomListItemResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatRoomMemberResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatRoomsPageResponse;
import com.sejong.userservice.domain.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.domain.chat.controller.response.RoomResponse;
import com.sejong.userservice.domain.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.domain.chat.controller.response.UnreadCountResponse;
import com.sejong.userservice.domain.chat.domain.ChatMessage;
import com.sejong.userservice.domain.chat.domain.ChatRoom;
import com.sejong.userservice.domain.chat.domain.ChatRoomUser;
import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.chat.repository.ChatMessageRepository;
import com.sejong.userservice.domain.chat.repository.ChatRoomRepository;
import com.sejong.userservice.domain.chat.repository.ChatRoomUserRepository;
import com.sejong.userservice.domain.chat.repository.projection.ChatRoomSummaryProjection;
import com.sejong.userservice.domain.chat.repository.projection.RoomCountProjection;
import com.sejong.userservice.domain.chat.repository.projection.RoomUnreadCountProjection;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.type.BaseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository; // TODO(sigmaith): refactoring 필요
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
        User friend = resolveUser(friendUsername);
        User owner = resolveUser(myUsername);

        if (friend.equals(owner)) throw new BaseException(DM_ROOM_WITH_OTHER_PERSON);

        // 1. ChatRoom 생성 및 저장
        ChatRoom chatRoom = ChatRoom.dmRoom(newRoomId);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 2. 단방향 관계: ChatRoomUser를 직접 생성하여 저장
        String ownerDisplayName = friend.getNickname() != null ? friend.getNickname() : friend.getUsername();
        String friendDisplayName = owner.getNickname() != null ? owner.getNickname() : owner.getUsername();
        chatRoomUserRepository.save(ChatRoomUser.join(savedRoom, owner, ChatRoomUserRole.OWNER, ownerDisplayName));
        chatRoomUserRepository.save(ChatRoomUser.join(savedRoom, friend, ChatRoomUserRole.MEMBER, friendDisplayName));

        return SingleRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse createGroupRoom(String newRoomId, String roomName, List<String> friendsUsername,
                                             String myUsername) {
        // 1. ChatRoom 생성 및 저장
        ChatRoom chatRoom = ChatRoom.groupRoom(newRoomId, roomName);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 2. 단방향 관계: 방장(myUsername)을 OWNER로 추가
        User owner = resolveUser(myUsername);
        ensureJoined(savedRoom, owner, ChatRoomUserRole.OWNER);

        // 3. 나머지 멤버들을 MEMBER로 추가 (요청값이 username/userId 혼재해도 동작)
        for (User friend : resolveUsers(friendsUsername)) {
            if (friend.equals(owner)) continue;
            ensureJoined(savedRoom, friend, ChatRoomUserRole.MEMBER);
        }

        return GroupRoomResponse.of(savedRoom.getRoomId(), savedRoom.getRoomName());
    }

    @Transactional
    public GroupRoomResponse addRoomMembers(String roomId, List<String> friendsUsername) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        // 단방향 관계: 새 멤버들을 ChatRoomUser로 직접 추가
        for (User user : resolveUsers(friendsUsername)) {
            ensureJoined(chatRoom, user, ChatRoomUserRole.MEMBER);
        }

        return GroupRoomResponse.of(chatRoom.getRoomId(), chatRoom.getRoomName());
    }

    @Transactional
    public RoomResponse quitRoom(String roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        User user = resolveUser(username);

        chatRoomUserRepository.deleteByUserIdAndRoomId(user.getId(), chatRoom.getRoomId());
        if (chatRoomUserRepository.getCountByRoomId(chatRoom.getRoomId()) == 0) {
            chatRoomRepository.deleteById(roomId);
        }
        return RoomResponse.quitOf(roomId);
    }

        @Transactional(readOnly = true)
        public ChatRoomsPageResponse findRoomsPage(
            String username,
            java.time.LocalDateTime cursorAt,
            String cursorRoomId,
            int size
        ) {
        User currentUser = resolveUser(username);

        int pageSize = Math.min(Math.max(size, 1), 50);
        String safeCursorRoomId = (cursorAt == null) ? null : cursorRoomId;

        // 1) 방 목록(최근 활동 기준) 페이지
        List<ChatRoomSummaryProjection> summaries = chatRoomUserRepository.findRoomSummariesPageByUserId(
            currentUser.getId(),
            cursorAt,
            safeCursorRoomId,
            PageRequest.of(0, pageSize)
        );
        List<String> roomIds = summaries.stream().map(ChatRoomSummaryProjection::getRoomId).toList();

        // next cursor
        java.time.LocalDateTime nextCursorAt = null;
        String nextCursorRoomId = null;
        if (!summaries.isEmpty() && summaries.size() == pageSize) {
            ChatRoomSummaryProjection last = summaries.get(summaries.size() - 1);
            nextCursorAt = last.getActivityAt();
            nextCursorRoomId = last.getRoomId();
        }

        if (roomIds.isEmpty()) {
            return ChatRoomsPageResponse.of(List.of(), cursorAt, cursorRoomId, null, null);
        }

        // 2) 참여자 정보 배치 로딩
        Map<String, List<ChatRoomUser>> membersByRoomId = chatRoomUserRepository.findAllByRoomIdsWithUser(roomIds)
            .stream()
            .collect(Collectors.groupingBy(it -> it.getChatRoom().getRoomId()));

        // 3) 멤버 수 배치 로딩
        Map<String, Long> memberCountByRoomId = chatRoomUserRepository.countMembersByRoomIds(roomIds)
            .stream()
            .collect(Collectors.toMap(RoomCountProjection::getRoomId, RoomCountProjection::getCount));

        // 4) 안읽은 메시지 수 배치 로딩
        Map<String, Long> unreadByRoomId = chatRoomUserRepository.countUnreadByRoomIds(currentUser.getId(), roomIds)
            .stream()
            .collect(Collectors.toMap(RoomUnreadCountProjection::getRoomId, RoomUnreadCountProjection::getUnreadCount));

        // 5) 최근 메시지 발신자 프로필 배치 로딩
        List<String> lastSenderUsernames = summaries.stream()
            .map(ChatRoomSummaryProjection::getLastSenderUsername)
            .filter(it -> it != null && !it.isBlank())
            .distinct()
            .toList();
        Map<String, User> lastSenderByUsername = userRepository.findByUsernameIn(lastSenderUsernames)
            .stream()
            .collect(Collectors.toMap(User::getUsername, Function.identity()));

        List<ChatRoomListItemResponse> items = summaries.stream()
            .map(summary -> {
                String roomId = summary.getRoomId();

                String roomName = summary.getRoomName();
                if (roomName == null) {
                roomName = summary.getDisplayName();
                }

                List<ChatRoomMemberResponse> others = membersByRoomId.getOrDefault(roomId, List.of())
                    .stream()
                    .map(ChatRoomUser::getUser)
                    .filter(u -> u != null && !u.getId().equals(currentUser.getId()))
                    .map(ChatRoomMemberResponse::of)
                    .toList();

                User lastSender = summary.getLastSenderUsername() == null
                    ? null
                    : lastSenderByUsername.get(summary.getLastSenderUsername());

                return ChatRoomListItemResponse.of(
                    roomId,
                    roomName,
                    summary.getLastMessageAt(),
                    summary.getLastMessage(),
                    summary.getLastMessageImageUrl(),
                    summary.getLastSenderUsername(),
                    lastSender == null ? null : lastSender.getNickname(),
                    lastSender == null ? null : lastSender.getRealName(),
                    lastSender == null ? null : lastSender.getProfileImageKey(),
                    memberCountByRoomId.getOrDefault(roomId, 0L),
                    unreadByRoomId.getOrDefault(roomId, 0L),
                    others
                );
            })
            .toList();

        return ChatRoomsPageResponse.of(items, cursorAt, cursorRoomId, nextCursorAt, nextCursorRoomId);
        }

    @Transactional(readOnly = true)
    public String findExistingDmRoomId(String myUsernameOrUserId, String friendUsername) {
        User me = resolveUser(myUsernameOrUserId);
        User friend = resolveUser(friendUsername);
        if (me.getId().equals(friend.getId())) {
            return null;
        }
        List<String> roomIds = chatRoomUserRepository.findExistingDmRoomIdsByUserIds(me.getId(), friend.getId());
        return roomIds.isEmpty() ? null : roomIds.get(0);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String roomId, String usernameOrUserId) {
        User user = resolveUser(usernameOrUserId);

        // 방 멤버가 아니면 조회 불가
        if (!chatRoomUserRepository.existsByUserIdAndRoomId(user.getId(), roomId)) {
            throw new BaseException(ROOM_ID_NOT_FOUND);
        }

        ChatRoomUser membership = chatRoomUserRepository.findByUserIdAndRoomId(user.getId(), roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        Long lastReadMessageId = membership.getLastReadMessageId();
        long unread = chatMessageRepository.countUnreadChatMessages(roomId, lastReadMessageId, user.getUsername());
        return UnreadCountResponse.of(roomId, unread, lastReadMessageId);
    }

    @Transactional
    public UnreadCountResponse markRoomAsRead(String roomId, String usernameOrUserId) {
        User user = resolveUser(usernameOrUserId);

        ChatRoomUser membership = chatRoomUserRepository.findByUserIdAndRoomId(user.getId(), roomId)
                .orElseThrow(() -> new BaseException(ROOM_ID_NOT_FOUND));

        Long latestReadableId = chatMessageRepository.findLatestReadableChatMessageId(roomId);
        membership.updateLastReadMessageId(latestReadableId);

        // 읽음 처리 직후 unread는 0 (정합성/편의상)
        return UnreadCountResponse.of(roomId, 0L, latestReadableId);
    }

    private User resolveUser(String usernameOrUserId) {
        if (usernameOrUserId == null || usernameOrUserId.isBlank()) {
            throw new BaseException(NOT_FOUND_USER);
        }

        return userRepository.findByUsername(usernameOrUserId)
                .or(() -> {
                    try {
                        Long userId = Long.parseLong(usernameOrUserId);
                        return userRepository.findById(userId);
                    } catch (NumberFormatException ignored) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    private Set<User> resolveUsers(List<String> usernamesOrUserIds) {
        if (usernamesOrUserIds == null || usernamesOrUserIds.isEmpty()) {
            return Set.of();
        }

        Set<User> users = new LinkedHashSet<>();
        for (String key : usernamesOrUserIds) {
            users.add(resolveUser(key));
        }
        return users;
    }

    private void ensureJoined(ChatRoom room, User user, ChatRoomUserRole role) {
        if (chatRoomUserRepository.existsByUserIdAndRoomId(user.getId(), room.getRoomId())) {
            return;
        }
        ChatRoomUser join = ChatRoomUser.join(room, user, role);
        chatRoomUserRepository.save(join);
    }

    public List<ChatMessageResponse> findAllChatMessages(String roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAllChatMessages(roomId);
        return chatMessages.stream()
                .map(it -> {
                    User user = userRepository.findByUsername(it.getUsername())
                            .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
                    return ChatMessageResponse.of(it, user.getNickname(), user.getProfileImageKey());
                }).toList();
    }

        @Transactional(readOnly = true)
        public ChatMessagesPageResponse findChatMessagesPage(String roomId, Long cursorId, int size) {
        int pageSize = Math.min(Math.max(size, 1), 100);
        List<ChatMessage> chatMessagesDesc = chatMessageRepository.findChatMessagesPage(
            roomId,
            cursorId,
            PageRequest.of(0, pageSize)
        );

            Long nextCursorId = null;
            if (!chatMessagesDesc.isEmpty() && chatMessagesDesc.size() == pageSize) {
                // DESC 정렬이므로 마지막 요소가 가장 오래된 메시지 -> 다음 페이지 cursor
                nextCursorId = chatMessagesDesc.get(chatMessagesDesc.size() - 1).getId();
            }

        // sender nickname 조회 최적화 (N+1 방지)
        List<String> usernames = chatMessagesDesc.stream()
            .map(ChatMessage::getUsername)
            .distinct()
            .toList();

        Map<String, User> userByUsername = userRepository.findByUsernameIn(usernames).stream()
            .collect(java.util.stream.Collectors.toMap(User::getUsername, Function.identity()));

        // 최신 메시지부터 보여주기 위해 DESC 순서를 그대로 반환
        List<ChatMessageResponse> items = chatMessagesDesc.stream()
            .map(message -> {
                User user = userByUsername.get(message.getUsername());
                String nickname = (user == null) ? null : user.getNickname();
                String thumbnailUrl = (user == null) ? null : user.getProfileImageKey();
                return ChatMessageResponse.of(message, nickname, thumbnailUrl);
            })
            .toList();

        return ChatMessagesPageResponse.of(items, cursorId, nextCursorId);
        }
}
