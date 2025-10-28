package com.sejong.userservice.application.chat.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupRoomRequest {
    private String roomName;
    private List<String> friendsUsername;
}
