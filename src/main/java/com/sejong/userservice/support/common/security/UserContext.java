package com.sejong.userservice.support.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserContext {

    private final String username;
    private final String role;

    public static UserContext of(String userId, String userRole) {
        return new UserContext(userId, userRole);
    }
}
