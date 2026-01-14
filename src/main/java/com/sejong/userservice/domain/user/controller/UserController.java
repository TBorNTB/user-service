package com.sejong.userservice.domain.user.controller;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.dto.request.BatchRoleUpdateRequest;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.LoginRequest;
import com.sejong.userservice.domain.user.dto.request.ResetPasswordRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRoleRequest;
import com.sejong.userservice.domain.user.dto.request.VerificationRequest;
import com.sejong.userservice.domain.user.dto.response.JoinResponse;
import com.sejong.userservice.domain.user.dto.response.LoginResponse;
import com.sejong.userservice.domain.user.dto.response.LikedPostResponse;
import com.sejong.userservice.domain.user.dto.response.UserActivityStatsResponse;
import com.sejong.userservice.domain.user.dto.response.UserCommentPostResponse;
import com.sejong.userservice.domain.user.dto.response.UserCountResponse;
import com.sejong.userservice.domain.user.dto.response.UserRes;
import com.sejong.userservice.domain.user.dto.response.UserRoleCountResponse;
import com.sejong.userservice.domain.user.dto.response.UserSearchResponse;
import com.sejong.userservice.domain.user.service.UserService;
import com.sejong.userservice.domain.user.service.VerificationService;
import com.sejong.userservice.support.common.pagination.CursorPageReq;
import com.sejong.userservice.support.common.pagination.CursorPageRes;
import com.sejong.userservice.support.common.pagination.OffsetPageReq;
import com.sejong.userservice.support.common.pagination.OffsetPageRes;
import com.sejong.userservice.support.common.security.UserContext;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final VerificationService verificationService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping
    public ResponseEntity<JoinResponse> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        JoinResponse response = userService.joinProcess(joinRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "사용자 로그인", description = "이메일과 패스워드로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(loginRequest, response);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "전체 사용자 조회 - 페이지네이션", description = "모든 사용자 목록을 조회합니다")
    @GetMapping("/page")
    public OffsetPageRes<List<UserRes>> getAllUsersPagination(
        @ModelAttribute @Valid OffsetPageReq offsetPageReq
    ) {
        Page<UserRes> allUsers = userService.getAllUsers(offsetPageReq.toPageable());
        return OffsetPageRes.ok(allUsers);
    }

    @Operation(summary = "전체 사용자 조회 - 커서 기반 페이지네이션", 
               description = "커서 기반 페이지네이션으로 모든 사용자 목록을 조회합니다. username, realname, nickname, profileImageUrl, email을 반환합니다.")
    @GetMapping("/search/cursor")
    public CursorPageRes<List<UserSearchResponse>> getAllUsersWithCursor(
        @ModelAttribute @Valid CursorPageReq cursorPageReq
    ) {
        return userService.getAllUsersWithCursor(cursorPageReq);
    }

    @Operation(summary = "사용자 검색 - 커서 기반 페이지네이션", 
               description = "nickname이나 realname으로 사용자를 검색합니다. 커서 기반 페이지네이션을 사용합니다.")
    @GetMapping("/search/cursor/name")
    public CursorPageRes<List<UserSearchResponse>> searchUsersByNicknameOrRealName(
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) String realName,
        @ModelAttribute @Valid CursorPageReq cursorPageReq
    ) {
        return userService.searchUsersByNicknameOrRealName(nickname, realName, cursorPageReq);
    }

    @Operation(summary = "role 및 이름으로 사용자 조회 - 페이지네이션", description = "해당 role을 가진 사용자 목록을 조회합니다. nickname, realName으로 검색 가능합니다.")
    @GetMapping("/search")
    public OffsetPageRes<List<UserRes>> getUsersByRoles(
            @RequestParam(required = false) List<UserRole> roles,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String realName,
            @ModelAttribute @Valid OffsetPageReq offsetPageReq
    ) {
        Page<UserRes> users = userService.getUsersByRoles(roles, nickname, realName, offsetPageReq.toPageable());
        return OffsetPageRes.ok(users);
    }

    @Operation(summary = "회원 role 수정 (어드민 전용 api)")
    @PatchMapping("role-update/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> updateUserRole(
            @PathVariable("id") Long id,
            @RequestBody UserUpdateRoleRequest userUpdateRoleRequest
    ) {
        UserContext currentUser = getCurrentUser();
        String message = userService.updateUserRole(id, userUpdateRoleRequest.getUserRole(), currentUser.getRole());
        return new ResponseEntity<>(message, HttpStatus.OK);
    }


    @Operation(summary = "사용자 정보 수정", description = "자신의 사용자 정보를 수정합니다 (회원 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    @PatchMapping
    public ResponseEntity<UserRes> updateUser(@RequestBody UserUpdateRequest updateRequest) {
        UserContext currentUser = getCurrentUser();
        UserRes updatedUser = userService.updateUser(currentUser.getUsername(), updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @Operation(summary = "사용자 탈퇴", description = "자신의 계정을 삭제합니다 (회원 또는 관리자 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'GUEST', 'ASSOCIATE_MEMBER')")
    @DeleteMapping
    public ResponseEntity<UserRes> deleteUser() {
        UserContext currentUser = getCurrentUser();
        UserRes userRes = userService.
                deleteUser(currentUser.getUsername());
        return new ResponseEntity<>(userRes, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 및 토큰 무효화 (회원 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'GUEST', 'ASSOCIATE_MEMBER')")
    @PostMapping("/logout")
    public ResponseEntity<UserRes> logout(
            HttpServletRequest request, HttpServletResponse response
    ) {
        UserContext currentUser = getCurrentUser();
        String username = currentUser.getUsername();

        if (username == null || username.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.getAccessTokenFromHeader(request);
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        userService.logout(accessToken, refreshToken);

        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setMaxAge(0);
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        response.addCookie(expiredCookie);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "자신의 역할 조회", description = "자신의 역할 조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'GUEST', 'ASSOCIATE_MEMBER')")
    @GetMapping("/role/one")
    public ResponseEntity<String> getUserRole() {
        UserContext currentUser = getCurrentUser();
        String role = currentUser.getRole();
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @Operation(summary = "자신의 프로파일 조회", description = "자신의 프로파일 조회")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'GUEST', 'ASSOCIATE_MEMBER')")
    @GetMapping("/profile")
    public ResponseEntity<UserRes> getUserProfile() {
        UserContext currentUser = getCurrentUser();
        String username = currentUser.getUsername();
        UserRes response = userService.getUserInfo(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "비밀번호를 재설정 하기 위해 이메일로 인증코드를 발송합니다.")
    @PostMapping("/auth/verification-code")
    public ResponseEntity<Map<String, String>> requestVerificationCode(
            @Valid @RequestBody VerificationRequest request
    ) {
        verificationService.sendVerificationCode(request);
        Map<String, String> response = Map.of("message", "인증코드 메일이 전송되었습니다.");
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "비밀번호 변경용 인증 코드 검증", description = "이메일 인증 코드를 검증하고 비밀번호를 변경합니다.")
    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        verificationService.verifyEmailCode(request);
        userService.resetPassword(request.getEmail(), request.getNewPassword());
        Map<String, String> response = Map.of("message", "비밀번호가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "신규 가입자 수 조회", 
               description = "특정 날짜(YYYY-MM-DD 형식) 이후에 가입한 유저 수를 반환합니다.")
    @GetMapping("/count/new")
    public ResponseEntity<UserCountResponse> getNewUserCount(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        Long count = userService.getNewUserCountSince(startDate);
        UserCountResponse response = UserCountResponse.builder()
                .count(count)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "역할별 회원 수 조회", 
               description = "각 역할(외부인, 준회원, 정회원, 선배님, 운영진)별 회원 수와 전체 회원 수를 반환합니다.")
    @GetMapping("/count/role")
    public ResponseEntity<UserRoleCountResponse> getUserRoleCounts() {
        UserRoleCountResponse response = userService.getUserRoleCounts();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "유저 등급 변경 (어드민 전용)", 
               description = "username 배열을 받아서 등급을 일괄 변경합니다. 단일 유저도 배열에 1개만 넣어서 사용 가능합니다.")
    @PatchMapping("/role/batch")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, String>> batchUpdateUserRole(
            @Valid @RequestBody BatchRoleUpdateRequest request
    ) {
        String message = userService.batchUpdateUserRole(request.getUsernames(), request.getRole());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @Operation(summary = "사용자 활동 통계 조회", 
               description = "현재 로그인한 사용자의 활동 통계를 반환합니다. 총 조회수, 받은 좋아요, 받은 댓글 수를 포함합니다.")
    @GetMapping("/activity/stats")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<UserActivityStatsResponse> getUserActivityStats() {
        UserContext currentUser = getCurrentUser();
        UserActivityStatsResponse response = userService.getUserActivityStats(currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요 누른 글 목록 조회", 
               description = "현재 로그인한 사용자가 좋아요를 누른 글의 postId와 postType 목록을 페이지네이션으로 반환합니다.")
    @GetMapping("/liked-posts")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public OffsetPageRes<List<LikedPostResponse>> getLikedPosts(
            @ModelAttribute @Valid OffsetPageReq offsetPageReq
    ) {
        UserContext currentUser = getCurrentUser();
        Page<LikedPostResponse> likedPosts = userService.getLikedPosts(
                currentUser.getUsername(), 
                offsetPageReq.toPageable()
        );
        return OffsetPageRes.ok(likedPosts);
    }

    @Operation(summary = "작성한 댓글이 있는 글 목록 조회", 
               description = "현재 로그인한 사용자가 작성한 댓글이 있는 글의 postId와 postType 목록을 페이지네이션으로 반환합니다. 중복 제거되어 각 글당 하나의 레코드만 반환됩니다.")
    @GetMapping("/commented-posts")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public OffsetPageRes<List<UserCommentPostResponse>> getCommentedPosts(
            @ModelAttribute @Valid OffsetPageReq offsetPageReq
    ) {
        UserContext currentUser = getCurrentUser();
        Page<UserCommentPostResponse> commentedPosts = userService.getCommentedPosts(
                currentUser.getUsername(), 
                offsetPageReq.toPageable()
        );
        return OffsetPageRes.ok(commentedPosts);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
