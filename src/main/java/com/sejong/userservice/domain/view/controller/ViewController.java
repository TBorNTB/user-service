package com.sejong.userservice.domain.view.controller;

import com.sejong.userservice.domain.view.dto.response.ViewCountResponse;
import com.sejong.userservice.domain.view.service.ViewService;
import com.sejong.userservice.support.common.constants.PostType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "조회수 API", description = "게시물 조회수 관련 API")
@RestController
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    @Operation(summary = "조회수 증가", description = "게시물 조회 시 조회수를 1 증가시킵니다 (중복 조회 방지 적용)")
    @PostMapping("/{postId}")
    public ResponseEntity<ViewCountResponse> increaseViewCount(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "postType") PostType postType,
            HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);
        ViewCountResponse response = viewService.increaseViewCount(postId, postType, clientIp);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "조회수 조회", description = "특정 게시물의 현재 조회수를 조회합니다")
    @GetMapping("/{postId}/count")
    public ResponseEntity<ViewCountResponse> getViewCount(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "postType") PostType postType
    ) {
        ViewCountResponse response = viewService.getViewCount(postId, postType);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}