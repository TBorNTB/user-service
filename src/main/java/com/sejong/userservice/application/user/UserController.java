package com.sejong.userservice.application.user;

import com.sejong.userservice.application.exception.UserNotFoundException;
import com.sejong.userservice.application.user.dto.CustomUserDetails;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.LoginRequest;
import com.sejong.userservice.application.user.dto.LoginResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.token.TokenType;
import com.sejong.userservice.infrastructure.common.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final TokenRepository tokenRepository;

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

    @Operation(summary = "사용자 로그인", description = "닉네임과 패스워드로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword(), 
                    null
            );

            Authentication authentication = authenticationManager.authenticate(authToken);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

            String email = customUserDetails.getEmail();

            // ------------ 권한 찾기 ------------
            // authentication 인증된 사용자 객체
            // getAuthorities() : 해당 사용자에게 부여된 권한(들)을 반환 (<- 여러개 일 수 있음: Collection<? extends GrantedAuthority>)
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();

            String role = auth.getAuthority();

            String accessToken = jwtUtil.createAccessToken(email, role);
            String refreshToken = jwtUtil.createRefreshToken(email);

            String accessJti = jwtUtil.getJti(accessToken);
            LocalDateTime accessExpiryDate = jwtUtil.getExpirationLocalDateTime(accessToken);
            String refreshJti = jwtUtil.getJti(refreshToken);
            LocalDateTime refreshExpiryDate = jwtUtil.getExpirationLocalDateTime(refreshToken);

            tokenRepository.revokeAllTokensForUser(email);
            tokenRepository.saveToken(accessToken, email, accessExpiryDate, accessJti, TokenType.ACCESS);
            tokenRepository.saveToken(refreshToken, email, refreshExpiryDate, refreshJti, TokenType.REFRESH);

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
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new UserNotFoundException("사용자 정보가 존재하지 않습니다.");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "사용자 정보 수정", description = "자신의 사용자 정보를 수정합니다 (회원 권한 필요)")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestBody UserUpdateRequest updateRequest) {

        UserResponse updatedUser = userService.updateUser(userDetails.getEmail(), updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @Operation(summary = "사용자 탈퇴", description = "자신의 계정을 삭제합니다 (회원 또는 관리자 권한 필요)")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN')")
    @DeleteMapping
    public ResponseEntity<UserResponse> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse userResponse = userService.deleteUser(userDetails.getEmail());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 및 토큰 무효화 (회원 권한 필요)")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/logout")
    public ResponseEntity<UserResponse> logoutUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String username = userDetails.getEmail();

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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{username}/admin")
    public ResponseEntity<UserResponse> grantAdminRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("username") String grantedUsername,
            HttpServletResponse response) {

        log.info("관리자 권한 부여 {}: {}가 {}에게 관리자 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getEmail(),
                grantedUsername);
        UserResponse userResponse = userService.grantAdminRole(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @Operation(summary = "정식 회원 승인", description = "특정 사용자를 정식 회원으로 승인합니다 (관리자 권한 필요)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{username}/confirm")
    public ResponseEntity<UserResponse> confirmMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("username") String grantedUsername) {

        log.info("정식회원 권한 부여 {}: {}가 {}에게 정식 회원 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getEmail(),
                grantedUsername);
        UserResponse userResponse = userService.confirmMember(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}
