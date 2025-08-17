package com.sejong.userservice.application.user;

import com.sejong.userservice.application.exception.UserNotFoundException;
import com.sejong.userservice.application.user.dto.CustomUserDetails;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkUser(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Boolean.TRUE);
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<JoinResponse> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        JoinResponse response = userService.joinProcess(joinRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new UserNotFoundException("사용자 정보가 존재하지 않습니다.");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestBody UserUpdateRequest updateRequest) {

        UserResponse updatedUser = userService.updateUser(userDetails.getUsername(), updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN')")
    @DeleteMapping
    public ResponseEntity<UserResponse> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse userResponse = userService.deleteUser(userDetails.getUsername());
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @PostMapping("/logout")
    public ResponseEntity<UserResponse> logoutUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String username = userDetails.getUsername();

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{username}/admin")
    public ResponseEntity<UserResponse> grantAdminRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("username") String grantedUsername,
            HttpServletResponse response) {

        log.info("관리자 권한 부여 {}: {}가 {}에게 관리자 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getUsername(),
                grantedUsername);
        UserResponse userResponse = userService.grantAdminRole(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{username}/confirm")
    public ResponseEntity<UserResponse> confirmMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("username") String grantedUsername) {

        log.info("정식회원 권한 부여 {}: {}가 {}에게 정식 회원 권한을 부여합니다. ", LocalDateTime.now(), userDetails.getUsername(),
                grantedUsername);
        UserResponse userResponse = userService.confirmMember(grantedUsername);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}
