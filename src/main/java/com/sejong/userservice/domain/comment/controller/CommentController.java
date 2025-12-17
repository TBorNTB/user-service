package com.sejong.userservice.domain.comment.controller;

import com.sejong.userservice.domain.comment.command.CommentCommand;
import com.sejong.userservice.domain.comment.dto.request.CommentReq;
import com.sejong.userservice.domain.comment.dto.response.CommentRes;
import com.sejong.userservice.domain.comment.service.CommentService;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.pagination.CursorPageReq;
import com.sejong.userservice.support.common.pagination.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "댓글 API", description = "게시물 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 게시물에 댓글을 작성합니다")
    @PostMapping("/{postId}")
    public ResponseEntity<CommentRes> createComment(
        @RequestHeader("X-User-Id") String username,
        @PathVariable(name = "postId") Long postId,
        @RequestParam(name = "postType") PostType postType,
        @Valid @RequestBody CommentReq request) {
        CommentCommand command = CommentCommand.of(username, postId, postType, request.getContent());
        CommentRes response = commentService.createComment(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시물의 댓글 목록을 커서 기반 페이징으로 조회합니다")
    @GetMapping("/{postId}")
    public CursorPageRes<List<CommentRes>>
    showComments(
        @RequestParam(name = "postType") PostType postType,
        @PathVariable(name = "postId") Long postId,
        @ParameterObject @Valid CursorPageReq cursorPageReq) {
        List<CommentRes> comments = commentService.getComments(cursorPageReq, postId, postType);
        return CursorPageRes.from(comments, cursorPageReq.getSize(), CommentRes::getId);
    }

    @Operation(summary = "대댓글 작성", description = "특정 댓글에 대댓글을 작성합니다")
    @PostMapping("/{postId}/reply/{parentId}")
    public ResponseEntity<CommentRes> createReply(
        @RequestHeader("X-User-Id") String username,
        @RequestParam(name = "postType") PostType postType,
        @PathVariable(name = "postId") Long postId,
        @PathVariable(name = "parentId") Long parentId,
        @Valid @RequestBody CommentReq request) {
        CommentCommand command = CommentCommand.ofReply(username, postId, postType, request.getContent(), parentId);
        CommentRes response = commentService.createComment(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 커서 기반 페이징으로 조회합니다")
    @GetMapping("/{commentId}/replies")
    public CursorPageRes<List<CommentRes>> showReplies(
        @PathVariable(name = "commentId") Long commentId,
        @ParameterObject @Valid CursorPageReq cursorPageReq) {
        return commentService.getReplies(commentId, cursorPageReq);
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentRes> updateComment(
        @RequestHeader("X-User-Id") String username,
        @PathVariable(name = "commentId") Long commentId,
        @Valid @RequestBody CommentReq request) {
        CommentRes response = commentService.updateComment(username, commentId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @RequestHeader("X-User-Id") String username,
        @PathVariable(name = "commentId") Long commentId) {
        commentService.deleteComment(username, commentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
