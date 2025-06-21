package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.api.controller.dto.UserResponse;
import com.sejong.userservice.api.controller.dto.UserUpdateRequest;
import com.sejong.userservice.application.service.UserService;
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
    public ResponseEntity<String> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        boolean success = userService.joinProcess(joinRequest);

        if (success) {
            return new ResponseEntity<>("Registration successful!", HttpStatus.CREATED); // 201 Created
        } else {
            // Handle specific error cases here, e.g., username already exists
            return new ResponseEntity<>("Registration failed: Username already exists or invalid data.", HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * 사용자 정보를 업데이트하는 PATCH 엔드포인트
     * @param username URL 경로에서 받을 사용자의 식별자 (username)
     * @param updateRequest 업데이트할 정보가 담긴 DTO
     * @return 업데이트된 사용자 정보 DTO 또는 에러 응답
     */
    @PatchMapping("/{username}") // PATCH /users/{username} 요청을 처리
    public ResponseEntity<UserResponse> updateUser(@PathVariable String username,
                                                   @RequestBody UserUpdateRequest updateRequest) {

        UserResponse updatedUser = userService.updateUser(username, updateRequest);

        if (updatedUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * 사용자 정보를 삭제하는 DELETE 엔드포인트
     * @param username URL 경로에서 받을 사용자의 식별자 (username)
     * @return 삭제 성공 여부에 따른 응답
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        // Todo. 실제 운영 환경에서는, 삭제 권한 확인 (예: 로그인한 사용자가 본인인지, 관리자인지) 로직이 추가되어야 합니다.
        // 현재는 예시를 위해 생략합니다.

        boolean success = userService.deleteUser(username);

        if (success) {
            // 204 No Content: 성공적으로 요청을 처리했지만, 응답 본문에 보낼 콘텐츠가 없음
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            // 404 Not Found: 삭제하려는 사용자를 찾을 수 없음
            // 또는 500 Internal Server Error: 삭제 실패 (예: DB 제약조건 위반 등)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 사용자가 없거나 삭제 실패
        }
    }

    /**
     * 사용자 로그아웃 엔드포인트
     * @param request 요청 (Refresh Token 쿠키를 제거하기 위함)
     * @param response 응답 (Refresh Token 쿠키를 제거하기 위함)
     * @return 로그아웃 성공 응답
     */
    @PostMapping("/logout") // POST /users/logout 요청을 처리
    public ResponseEntity<Void> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // 실제로는 Access Token에서 사용자 정보를 추출해야 합니다.
        // 현재는 편의상 request.getHeader("X-Authenticated-User")를 사용하거나,
        // JWT 필터에서 SecurityContext에 저장된 Authentication을 이용해야 합니다.
        // 여기서는 예시를 위해 임시로 Access Token에서 username을 추출한다고 가정합니다.
        // JWT 필터를 통한 인증 후, SecurityContextHolder.getContext().getAuthentication().getName() 등으로 username을 가져오는 것이 이상적입니다.

        // TODO: 로그인된 사용자의 username을 SecurityContext에서 가져오는 로직 추가
        String username = "현재 로그인된 사용자 이름"; // 실제로는 SecurityContext에서 가져와야 함.
        // 예: SecurityContextHolder.getContext().getAuthentication().getName();

        if (username.equals("현재 로그인된 사용자 이름")) { // 실제 username을 가져오지 못했을 경우
            // Access Token 유효성 검사 및 사용자 이름 추출하는 별도 필터/로직이 필요
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        boolean success = userService.logoutUser(username);

        if (success) {
            // Refresh Token 쿠키를 만료시켜 클라이언트에서도 제거
            Cookie expiredCookie = new Cookie("refreshToken", null);
            expiredCookie.setMaxAge(0);
            expiredCookie.setHttpOnly(true);
            expiredCookie.setPath("/");
            response.addCookie(expiredCookie);
            return new ResponseEntity<>(HttpStatus.OK); // 200 OK 또는 204 No Content
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 로그아웃 처리 실패
        }
    }
}
