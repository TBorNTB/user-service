package com.sejong.userservice.application.chat.controller;

import com.sejong.userservice.application.chat.controller.request.GroupRoomRequest;
import com.sejong.userservice.application.chat.controller.request.SingleRoomRequest;
import com.sejong.userservice.application.chat.controller.response.ChatMessageResponse;
import com.sejong.userservice.application.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.application.chat.controller.response.RoomResponse;
import com.sejong.userservice.application.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.application.chat.service.ChatService;
import com.sejong.userservice.application.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        SingleRoomResponse response = chatService.createRoom(newRoomId, request.getFriendUsername(), currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 채팅방 만드는 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @PostMapping("/rooms")
    public ResponseEntity<GroupRoomResponse> createGroupRoom(@RequestBody GroupRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        GroupRoomResponse response = chatService.createRooms(newRoomId, request.getRoomName()
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
        GroupRoomResponse response = chatService.addRoomMembers(roomId, request.getRoomName(), request.getFriendsUsername());
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
    public ResponseEntity<List<GroupRoomResponse>> getAllRooms() {
        UserContext currentUser = getCurrentUser();
        List<GroupRoomResponse> response = chatService.findAllRooms(currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //todo 커서기반으로 리팩터링 해야 될듯??
    @Operation(summary="채팅기록 전체조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    @GetMapping("/rooms/{roomId}/chat")
    public ResponseEntity<List<ChatMessageResponse>> getAllChatMessages(
            @PathVariable("roomId") String roomId
    ){
        List<ChatMessageResponse> response = chatService.findAllChatMessages(roomId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
