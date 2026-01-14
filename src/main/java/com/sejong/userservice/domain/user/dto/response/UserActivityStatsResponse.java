package com.sejong.userservice.domain.user.dto.response;

import lombok.Builder;

@Builder
public record UserActivityStatsResponse(
        Long totalPostCount,      // 작성한 글 개수
        Long totalViewCount,      // 총 조회수
        Long totalLikeCount,       // 받은 좋아요
        Long totalCommentCount     // 받은 댓글
) {
}

