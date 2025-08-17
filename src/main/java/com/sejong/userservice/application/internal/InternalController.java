package com.sejong.userservice.application.internal;

import com.sejong.userservice.application.user.UserService;
import com.sejong.userservice.application.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users/")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkUser(@PathVariable Long userId) {
        boolean exists = userService.exist(userId);
        return ResponseEntity.ok(exists);
    }

//    @GetMapping("/users")
//    public ResponseEntity<List<String>> getUsers() {
//        //todo
//    }
}
