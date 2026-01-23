package com.sejong.userservice.domain.chat.controller;

import com.sejong.userservice.domain.chat.controller.request.GroupRoomRequest;
import com.sejong.userservice.domain.chat.controller.request.SingleRoomRequest;
import com.sejong.userservice.domain.chat.controller.response.ChatMessageResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatMessagesPageResponse;
import com.sejong.userservice.domain.chat.controller.response.ChatRoomsPageResponse;
import com.sejong.userservice.domain.chat.controller.response.DmRoomLookupResponse;
import com.sejong.userservice.domain.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.domain.chat.controller.response.RoomResponse;
import com.sejong.userservice.domain.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.domain.chat.controller.response.UnreadCountResponse;
import com.sejong.userservice.domain.chat.service.ChatService;
import com.sejong.userservice.support.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "1대1 채팅방 만드는 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @PostMapping("/room")
    public ResponseEntity<SingleRoomResponse> createSingleRoom(@RequestBody SingleRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        SingleRoomResponse response = chatService.createDMRoom(newRoomId, request.getFriendUsername(), currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "특정 유저와의 1:1 채팅방 존재 여부 조회 (있으면 roomId, 없으면 null)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @GetMapping("/room")
    public ResponseEntity<DmRoomLookupResponse> getExistingDmRoomId(
            @RequestParam(name = "friendUsername") String friendUsername
    ) {
        UserContext currentUser = getCurrentUser();
        String roomId = chatService.findExistingDmRoomId(currentUser.getUsername(), friendUsername);
        return ResponseEntity.status(HttpStatus.OK).body(DmRoomLookupResponse.of(roomId));
    }

    @Operation(summary = "그룹 채팅방 만드는 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @PostMapping("/rooms")
    public ResponseEntity<GroupRoomResponse> createGroupRoom(@RequestBody GroupRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        GroupRoomResponse response = chatService.createGroupRoom(newRoomId, request.getRoomName()
                , request.getFriendsUsername(), currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "인원 추가 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @PutMapping("/rooms/{roomId}/members")
    public ResponseEntity<GroupRoomResponse> addGroupRoomMembers(
            @PathVariable(name = "roomId") String roomId,
            @RequestBody GroupRoomRequest request
    ) {
        GroupRoomResponse response = chatService.addRoomMembers(roomId, request.getFriendsUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "채팅방 나가기")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<RoomResponse> quitRoom(
            @PathVariable("roomId") String roomId
    ) {
        UserContext currentUser = getCurrentUser();
        RoomResponse response = chatService.quitRoom(roomId, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //todo 커서기반으로 리팩터링 해야 될듯??
    @Operation(summary = "한 유저의 방 전체조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @GetMapping("/rooms")
    public ResponseEntity<ChatRoomsPageResponse> findRoomsPage(
            @RequestParam(value = "cursorAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorAt,
            @RequestParam(value = "cursorRoomId", required = false) String cursorRoomId,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        UserContext currentUser = getCurrentUser();
        return ResponseEntity.ok(chatService.findRoomsPage(currentUser.getUsername(), cursorAt, cursorRoomId, size));
    }

    //todo 커서기반으로 리팩터링 해야 될듯??
    @Operation(summary="채팅기록 전체조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @GetMapping("/rooms/{roomId}/chat")
    public ResponseEntity<ChatMessagesPageResponse> getAllChatMessages(
            @PathVariable("roomId") String roomId,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
        ChatMessagesPageResponse response = chatService.findChatMessagesPage(roomId, cursorId, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "특정 채팅방 안읽은 메시지 개수 조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @PathVariable("roomId") String roomId
    ) {
        UserContext currentUser = getCurrentUser();
        UnreadCountResponse response = chatService.getUnreadCount(roomId, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "특정 채팅방 읽음 처리 (lastReadMessageId 갱신)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<UnreadCountResponse> markRoomAsRead(
            @PathVariable("roomId") String roomId
    ) {
        UserContext currentUser = getCurrentUser();
        UnreadCountResponse response = chatService.markRoomAsRead(roomId, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    // todo: 채팅방 삭제 기능 (chatroom 삭제시, chatroomuser 우선 삭제 필수)
}
