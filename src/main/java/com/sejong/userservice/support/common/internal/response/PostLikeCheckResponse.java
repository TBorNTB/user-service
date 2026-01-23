package com.sejong.userservice.support.common.internal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLikeCheckResponse {
    private String ownerUsername;
    private boolean isStored;
}
