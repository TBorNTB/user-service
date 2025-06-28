package com.sejong.userservice.application.user;

import com.sejong.userservice.application.exception.UserNotFoundException;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<JoinResponse> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        JoinResponse response = userService.joinProcess(joinRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new UserNotFoundException("사용자 정보가 존재하지 않습니다.");
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@RequestHeader("X-User-ID") String username,
                                                   @RequestBody UserUpdateRequest updateRequest) {

        UserResponse updatedUser = userService.updateUser(username, updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<UserResponse> deleteUser(@RequestHeader("X-User-ID") String username) {
        UserResponse userResponse = userService.deleteUser(username);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<UserResponse> logoutUser(
            @RequestHeader(value = "X-User-ID", required = false) String username,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

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

    @PatchMapping("/{username}/admin")
    public ResponseEntity<UserResponse> grantAdminRole(@PathVariable("username") String username,
                                                       HttpServletResponse response) {
        UserResponse userResponse = userService.grantAdminRole(username);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PatchMapping("/{username}/confirm")
    public ResponseEntity<UserResponse> confirmMember(@PathVariable("username") String username) {
        UserResponse userResponse = userService.confirmMember(username);

        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}
