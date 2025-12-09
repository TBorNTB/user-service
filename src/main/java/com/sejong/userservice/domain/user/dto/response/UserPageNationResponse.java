package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.domain.user.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class UserPageNationResponse {
    int size;
    int page;
    Long totalElements;
    private List<UserResponse> userResponses;

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
