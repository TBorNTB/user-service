package com.sejong.userservice.domain.view.dto.response;

import lombok.Builder;

@Builder
public record ViewCountResponse (
        Long viewCount
){
}