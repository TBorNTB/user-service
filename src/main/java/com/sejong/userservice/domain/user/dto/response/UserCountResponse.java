package com.sejong.userservice.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UserCountResponse(
        Long count
) {
}

