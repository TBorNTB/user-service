package com.sejong.userservice.application.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinResponse {
    private String username;
    private String message;

    public static JoinResponse of(String username, String message) {
        return new JoinResponse(username, message);
    }
}