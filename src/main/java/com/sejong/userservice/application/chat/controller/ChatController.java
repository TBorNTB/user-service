package com.sejong.userservice.application.chat.controller;

import com.sejong.userservice.application.chat.controller.request.GroupRoomRequest;
import com.sejong.userservice.application.chat.controller.request.SingleRoomRequest;
import com.sejong.userservice.application.chat.controller.response.GroupRoomResponse;
import com.sejong.userservice.application.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.application.chat.service.ChatService;
import com.sejong.userservice.application.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "1대1 채팅방 만드는 api")
    @PostMapping("/room")
    public ResponseEntity<SingleRoomResponse> createSingleRoom(@RequestBody SingleRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        SingleRoomResponse response = chatService.createRoom(newRoomId, request.getFriendUsername(), currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 채팅방 만드는 api")
    @PostMapping("/rooms")
    public ResponseEntity<GroupRoomResponse> createGroupRoom(@RequestBody GroupRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        GroupRoomResponse response = chatService.createRooms(newRoomId, request.getRoomName()
                , request.getFriendsUsername(), currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "인원 추가 api")
    @PutMapping("/rooms/{roomId}/members")
    public ResponseEntity<GroupRoomResponse> addGroupRoomMembers(
            @PathVariable(name = "roomId") String roomId,
            @RequestBody GroupRoomRequest request
    ) {
        GroupRoomResponse response = chatService.addRoomMembers(roomId, request.getRoomName(), request.getFriendsUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private UserContext getCurrentUser() {
        return UserContext.of("임시유저네임", "ADMIN");
//        return (UserContext) SecurityContextHolder.getContext()
//                .getAuthentication().getPrincipal();
    }
}
