package com.sejong.userservice.domain.like.dto.response;

import com.sejong.userservice.domain.like.domain.LikeStatus;
import lombok.Builder;

@Builder
public record LikeRes(Long likeCount, LikeStatus status) {
    public static LikeRes of(LikeStatus status, Long likeCount) {
        return LikeRes
                .builder()
                .likeCount(likeCount)
                .status(status)
                .build();
    }
}
