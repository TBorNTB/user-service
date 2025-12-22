package com.sejong.userservice.domain.comment.command;

import com.sejong.userservice.domain.comment.domain.Comment;
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
public class CommentCommand {
    private String username;
    private Long postId;
    private PostType postType;
    private String content;
    private Long parentId;

    public static CommentCommand of(String username, Long postId, PostType postType, String content) {
        return CommentCommand.builder()
                .username(username)
                .postId(postId)
                .postType(postType)
                .content(content)
                .parentId(null)
                .build();
    }

    public static CommentCommand ofReply(String username, Long postId, PostType postType, String content, Long parentId) {
        return CommentCommand.builder()
                .username(username)
                .postId(postId)
                .postType(postType)
                .content(content)
                .parentId(parentId)
                .build();
    }

    public static Comment toComment(CommentCommand command, Comment parent) {
        int depth = (parent != null) ? parent.getDepth() + 1 : 0;
        return Comment.builder()
                .content(command.getContent())
                .username(command.getUsername())
                .postId(command.getPostId())
                .postType(command.getPostType())
                .parent(parent)
                .depth(depth)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isReply() {
        return parentId != null;
    }
}
