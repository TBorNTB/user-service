package com.sejong.userservice.domain.comment.dto.response;

import com.sejong.userservice.domain.comment.domain.Comment;
import com.sejong.userservice.domain.user.dto.response.UserInfo;
import com.sejong.userservice.support.common.constants.PostType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRes {

    private Long id;
    private PostType postType;
    private Long postId;

    private UserInfo user;
    private String content;

    private Long parentId;
    private int depth;
    private int replyCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentRes from(Comment comment) {
        return CommentRes.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(UserInfo.missing(comment.getUsername()))
                .postId(comment.getPostId())
                .postType(comment.getPostType())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .replyCount(comment.getChildren() != null ? comment.getChildren().size() : 0)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public static CommentRes from(Comment comment, UserInfo userInfo) {
        return CommentRes.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userInfo != null ? userInfo : UserInfo.missing(comment.getUsername()))
                .postId(comment.getPostId())
                .postType(comment.getPostType())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .replyCount(comment.getChildren() != null ? comment.getChildren().size() : 0)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
