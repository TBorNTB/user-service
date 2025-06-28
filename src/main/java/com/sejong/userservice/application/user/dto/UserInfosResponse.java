package com.sejong.userservice.application.user.dto;

import com.sejong.userservice.core.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfosResponse {
    private List<UserInfoResponse> userInfos;

    public static UserInfosResponse from(List<User> users) {
        List<UserInfoResponse> userInfoResponseList = users.stream()
                .map(UserInfoResponse::from)
                .toList();

        return UserInfosResponse.builder()
                .userInfos(userInfoResponseList)
                .build();
    }
}