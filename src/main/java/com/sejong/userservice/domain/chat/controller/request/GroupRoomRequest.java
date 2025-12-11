package com.sejong.userservice.domain.chat.controller.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupRoomRequest {
    private String roomName;
    private List<String> friendsUsername;
}
