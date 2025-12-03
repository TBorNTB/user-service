package com.sejong.userservice.application.user.dto;

import com.sejong.userservice.core.user.User;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class UserPageNationResponse {
    private List<UserResponse> userResponses;
    int size;
    int page;
    Long totalElements;

    public static UserPageNationResponse from(Page<User> userPage) {
        List<User> content = userPage.getContent();
        List<UserResponse> userResponses = content.stream()
                .map(UserResponse::from)
                .toList();

        return UserPageNationResponse.builder()
                .userResponses(userResponses)
                .size(userPage.getTotalPages())
                .page(userPage.getNumber())
                .totalElements(userPage.getTotalElements())
                .build();
    }
}
