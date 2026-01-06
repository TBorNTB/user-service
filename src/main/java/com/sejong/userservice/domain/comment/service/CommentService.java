package com.sejong.userservice.domain.comment.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.DEPTH_LIMIT_EXCEEDED;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_COMMENT;

import com.sejong.userservice.domain.comment.domain.Comment;
import com.sejong.userservice.domain.comment.dto.command.CommentCommand;
import com.sejong.userservice.domain.comment.dto.request.CommentReq;
import com.sejong.userservice.domain.comment.dto.response.CommentRes;
import com.sejong.userservice.domain.comment.repository.CommentRepository;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.kafka.EventPublisher;
import com.sejong.userservice.support.common.pagination.CursorPageReq;
import com.sejong.userservice.support.common.pagination.CursorPageRes;
import com.sejong.userservice.support.common.pagination.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private static final int MAX_DEPTH = 1;

    private final CommentRepository commentRepository;
    private final PostInternalFacade postInternalFacade;
    private final EventPublisher eventPublisher;

    @Transactional
    public CommentRes createComment(CommentCommand command) {
        Comment parent = null;

        if (command.isReply()) {
            parent = commentRepository.findById(command.getParentId())
                    .orElseThrow(() -> new BaseException(NOT_FOUND_COMMENT));
            validateDepthLimit(parent);
        }

        String ownerUsername = postInternalFacade.checkPostExistenceAndOwner(
                command.getPostId(), command.getPostType());

        Comment comment = CommentCommand.toComment(command, parent);
        comment = commentRepository.save(comment);

        publishAlarm(comment, parent, ownerUsername);
        return CommentRes.from(comment);
    }

    private void validateDepthLimit(Comment parent) {
        if (parent.getDepth() >= MAX_DEPTH) {
            throw new BaseException(DEPTH_LIMIT_EXCEEDED);
        }
    }

    private void publishAlarm(Comment comment, Comment parent, String ownerUsername) {
        if (parent != null) {
            eventPublisher.publishReplyAlarm(parent, comment);
            return;
        }
        eventPublisher.publishCommentAlarm(comment, ownerUsername);
    }

    @Transactional(readOnly = true)
    public List<CommentRes> getComments(CursorPageReq cursorPageReq, Long postId,
                                        PostType postType) {
        Long cursorId = cursorPageReq.getCursorId();

        Pageable pageable = PageRequest.of(0, cursorPageReq.getSize() + 1);
        List<Comment> comments;
        if (cursorPageReq.getDirection() == SortDirection.ASC) {
            comments = commentRepository.findAllCommentsAsc(postId, postType, cursorId, pageable);
        } else {
            comments = commentRepository.findAllCommentsDesc(postId, postType, cursorId, pageable);
        }

        return comments.stream().map(CommentRes::from).toList();
    }

    @Transactional(readOnly = true)
    public CursorPageRes<List<CommentRes>> getReplies(Long parentId, CursorPageReq cursorPageReq) {
        Long cursorId = cursorPageReq.getCursorId();
        Pageable pageable = PageRequest.of(0, cursorPageReq.getSize() + 1);

        List<Comment> replies;
        if (cursorPageReq.getDirection() == SortDirection.ASC) {
            replies = commentRepository.findRepliesAsc(parentId, cursorId, pageable);
        } else {
            replies = commentRepository.findRepliesDesc(parentId, cursorId, pageable);
        }

        List<CommentRes> response = replies.stream().map(CommentRes::from).toList();
        return CursorPageRes.from(response, cursorPageReq.getSize(), CommentRes::getId);
    }

    @Transactional
    public CommentRes updateComment(String username, Long commentId, CommentReq request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COMMENT));
        comment.validateWriter(username);
        comment.update(request.getContent(), LocalDateTime.now());
        return CommentRes.from(comment);
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COMMENT));
        comment.validateWriter(username);
        commentRepository.deleteById(commentId);
    }
}
