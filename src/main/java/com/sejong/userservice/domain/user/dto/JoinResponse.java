package com.sejong.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JoinResponse {
    private String nickname;
    private String message;

    public static JoinResponse of(String nickname, String message) {
        return new JoinResponse(nickname, message);
    }
}