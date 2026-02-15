package com.sejong.userservice.domain.user.dto.response;

public record UserNameInfo(String nickname, String realName, String profileImageUrl) {
    public static UserNameInfo missing() {
        return new UserNameInfo(null, null, null);
    }
}