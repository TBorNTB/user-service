package com.sejong.userservice.domain.like.controller;

import com.sejong.userservice.domain.like.dto.response.LikeCountRes;
import com.sejong.userservice.domain.like.dto.response.LikeRes;
import com.sejong.userservice.domain.like.service.LikeService;
import com.sejong.userservice.support.common.constants.PostType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요 API", description = "게시물 좋아요 관련 API")
@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    @Operation(summary = "좋아요 토글 액션")
    public ResponseEntity<LikeRes> toggleLike(
            @RequestHeader("X-User-Id") String username,
            @PathVariable(name="postId") Long postId,
            @RequestParam(name="postType") PostType postType
    ){
        LikeRes likeRes = likeService.toggleLike(username, postId, postType);
        return ResponseEntity.ok(likeRes);
    }

    @GetMapping("/{postId}/me")
    @Operation(summary = "해당 포스트에 대한 유저의 좋아요 여부 및 좋아요 수")
    public ResponseEntity<LikeRes> getLike(
            @RequestHeader("X-User-Id") String username,
            @PathVariable(name="postId") Long postId,
            @RequestParam(name="postType") PostType postType
    ){
        LikeRes response = likeService.getLikeStatus(username, postId, postType);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{postId}/count")
    @Operation(summary = "해당 글에 대한 좋아요 수")
    public ResponseEntity<LikeCountRes> getLikeCount(
            @PathVariable(name="postId") Long postId,
            @RequestParam(name="postType") PostType postType
    ){
        LikeCountRes response = likeService.getLikeCount(postId, postType);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
