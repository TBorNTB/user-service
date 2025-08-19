package com.sejong.userservice.application.user;

import com.sejong.userservice.application.common.security.UserContext;
import com.sejong.userservice.application.common.security.jwt.JWTUtil;
import com.sejong.userservice.application.common.security.oauth.dto.CustomOAuth2User;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.LoginRequest;
import com.sejong.userservice.application.user.dto.LoginResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.token.TokenType;
import com.sejong.userservice.core.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Operation(summary = "헬스 체크", description = "서비스 상태를 확인합니다")
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @PostMapping
    public ResponseEntity<JoinResponse> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        JoinResponse response = userService.joinProcess(joinRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "사용자 로그인", description = "이메일과 패스워드로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            // 사용자 정보 조회
            User user = userService.findByEmail(loginRequest.getEmail());
            if (user == null) {
                return ResponseEntity.status(401).body(new LoginResponse("로그인 실패: 사용자를 찾을 수 없습니다.", null));
            }

            // 비밀번호 검증
            if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getEncryptPassword())) {
                return ResponseEntity.status(401).body(new LoginResponse("로그인 실패: 잘못된 비밀번호입니다.", null));
            }

            String username = user.getUsername();
            String role = user.getRole().name();

            String accessToken = jwtUtil.createAccessToken(username, role);
            String refreshToken = jwtUtil.createRefreshToken(username);

            String accessJti = jwtUtil.getJti(accessToken);
            LocalDateTime accessExpiryDate = jwtUtil.getExpirationLocalDateTime(accessToken);
            String refreshJti = jwtUtil.getJti(refreshToken);
            LocalDateTime refreshExpiryDate = jwtUtil.getExpirationLocalDateTime(refreshToken);

            tokenRepository.revokeAllTokensForUser(username);
            tokenRepository.saveToken(accessToken, username, accessExpiryDate, accessJti, TokenType.ACCESS);
            tokenRepository.saveToken(refreshToken, username, refreshExpiryDate, refreshJti, TokenType.REFRESH);

            // Access Token을 쿠키에 저장 (API Gateway에서 읽기 위해)
            Cookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);
            response.addCookie(accessTokenCookie);

            // Refresh Token을 HttpOnly 쿠키로 설정
            Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(new LoginResponse("로그인 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new LoginResponse("로그인 실패: " + e.getMessage(), null));
        }
    }

    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 조회합니다 (회원 권한 필요)")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "사용자 정보 수정", description = "자신의 사용자 정보를 수정합니다 (회원 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'OUTSIDER')")
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserUpdateRequest updateRequest) {
        UserContext currentUser = getCurrentUser();

        UserResponse updatedUser = userService.updateUser(currentUser.getUsername(), updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @Operation(summary = "사용자 탈퇴", description = "자신의 계정을 삭제합니다 (회원 또는 관리자 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'OUTSIDER', 'ASSOCIATE_MEMBER')")
    @DeleteMapping
    public ResponseEntity<UserResponse> deleteUser(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        UserContext currentUser = getCurrentUser();


        UserResponse userResponse = userService.deleteUser(userDetails.getName());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 및 토큰 무효화 (회원 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'OUTSIDER', 'ASSOCIATE_MEMBER')")
    @PostMapping("/logout")
    public ResponseEntity<UserResponse> logoutUser(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String username = userDetails.getName();

        if (username == null || username.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserResponse userResponse = userService.logoutUser(username);

        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setMaxAge(0);
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        response.addCookie(expiredCookie);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @Operation(summary = "관리자 권한 부여", description = "특정 사용자에게 관리자 권한을 부여합니다 (관리자 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/admin")
    public ResponseEntity<UserResponse> grantAdminRole(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("username") String grantedUsername,
            HttpServletResponse response) {

        log.info("관리자 권한 부여 {}: {}가 {}에게 관리자 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getName(),
                grantedUsername);
        UserResponse userResponse = userService.grantAdminRole(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @Operation(summary = "정식 회원 승인", description = "특정 사용자를 정식 회원으로 승인합니다 (관리자 권한 필요)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/confirm")
    public ResponseEntity<UserResponse> confirmMember(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("username") String grantedUsername) {

        log.info("정식회원 권한 부여 {}: {}가 {}에게 정식 회원 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getName(),
                grantedUsername);
        UserResponse userResponse = userService.confirmMember(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

//    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다")
//    @PostMapping("/token/refresh")
//    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
//
//            if (refreshToken == null) {
//                return ResponseEntity.status(401).body(new LoginResponse("리프레시 토큰이 없습니다.", null));
//            }
//
//            if (jwtUtil.isExpired(refreshToken)) {
//                return ResponseEntity.status(401).body(new LoginResponse("리프레시 토큰이 만료되었습니다.", null));
//            }
//
//            // 리프레시 토큰에서 사용자 정보 추출
//            String username = jwtUtil.getUsername(refreshToken);
//            User user = userService.findByEmail(username);
//
//            if (user == null) {
//                return ResponseEntity.status(401).body(new LoginResponse("유효하지 않은 사용자입니다.", null));
//            }
//
//            // 새로운 액세스 토큰 생성
//            String newAccessToken = jwtUtil.createAccessToken(username, user.getRole().name());
//
//            // 새로운 액세스 토큰 쿠키 설정
//            Cookie newAccessTokenCookie = jwtUtil.createAccessTokenCookie(newAccessToken);
//            response.addCookie(newAccessTokenCookie);
//
//            return ResponseEntity.ok(new LoginResponse("토큰 갱신 성공", null));
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body(new LoginResponse("토큰 갱신 실패: " + e.getMessage(), null));
//        }
//    }
}
