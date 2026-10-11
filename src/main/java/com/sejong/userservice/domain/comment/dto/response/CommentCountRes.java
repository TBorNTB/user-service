package com.sejong.userservice.domain.comment.dto.response;

import com.sejong.userservice.domain.comment.repository.CommentRepository;

public record CommentCountRes(Long postId, Long count) {

    public static CommentCountRes from(CommentRepository.PostCommentCount count) {
        return new CommentCountRes(count.getPostId(), count.getCount());
    }
}
