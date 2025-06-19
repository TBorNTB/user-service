package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.api.controller.dto.UserResponse;
import com.sejong.userservice.application.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
