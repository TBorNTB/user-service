package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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