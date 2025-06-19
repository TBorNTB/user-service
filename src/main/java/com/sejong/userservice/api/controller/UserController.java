package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.api.controller.dto.UserResponse;
import com.sejong.userservice.api.controller.dto.UserUpdateRequest;
import com.sejong.userservice.application.service.UserService;
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
}
