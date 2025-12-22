package com.sejong.userservice.support.common.kafka;

import com.sejong.userservice.support.common.constants.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLikeEvent {
    private Long postId;
    private PostType postType;
    private Long likeCount;

    public static PostLikeEvent of(PostType postType, Long postId, Long likeCount) {
        return PostLikeEvent.builder()
                .postType(postType)
                .postId(postId)
                .likeCount(likeCount)
                .build();
    }
}
