package com.sejong.userservice.application.chat.controller;

import com.sejong.userservice.application.chat.controller.request.SingleRoomRequest;
import com.sejong.userservice.application.chat.controller.response.SingleRoomResponse;
import com.sejong.userservice.application.chat.service.ChatService;
import com.sejong.userservice.application.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<SingleRoomResponse> createSingleRoom(@RequestBody SingleRoomRequest request) {
        String newRoomId = UUID.randomUUID().toString();
        UserContext currentUser = getCurrentUser();
        SingleRoomResponse response = chatService.createRoom(newRoomId, request.getFriendUsername(),currentUser.getUsername());
        return ResponseEntity.ok(response);
    }


    private UserContext getCurrentUser() {
        return UserContext.of("임시유저네임","ADMIN");
//        return (UserContext) SecurityContextHolder.getContext()
//                .getAuthentication().getPrincipal();
    }
}
