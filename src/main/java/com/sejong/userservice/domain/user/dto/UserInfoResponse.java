package com.sejong.userservice.domain.user.dto;

import com.sejong.userservice.domain.user.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponse {
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .name(user.getRealName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
