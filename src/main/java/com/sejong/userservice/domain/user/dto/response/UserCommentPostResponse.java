package com.sejong.userservice.domain.user.dto.response;

import com.sejong.userservice.support.common.constants.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCommentPostResponse {
    private PostType postType;
    private Long postId;
}

