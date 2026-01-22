package com.sejong.userservice.domain.user.controller;

import com.sejong.userservice.domain.user.dto.response.UserRes;
import com.sejong.userservice.domain.user.service.UserService;
import com.sejong.userservice.support.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @Operation(summary = "프로필 이미지 수정", description = "자신의 프로필 이미지를 업로드합니다.(회원 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserRes> updateProfileImage(@RequestPart("file") MultipartFile file) {
        UserContext currentUser = getCurrentUser();
        UserRes updatedUser = userService.updateProfileImage(currentUser.getUsername(), file);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }
}
