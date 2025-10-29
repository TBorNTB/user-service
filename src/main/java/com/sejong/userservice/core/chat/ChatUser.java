package com.sejong.userservice.core.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatUser {
    private String username;
    private String roomId;

    public static ChatUser of(String friendUsername, String newRoomId) {
        return ChatUser.builder()
                .username(friendUsername)
                .roomId(newRoomId)
                .build();
    }

    public static List<ChatUser> makeChatUsers(List<String> usernames, String newRoomId){
        return usernames.stream()
                .map(username -> ChatUser.of(username, newRoomId))
                .toList();
    }
}
