package com.sejong.userservice.application.internal;

import com.sejong.userservice.application.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalController {

    private final UserService userService;

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable("userId") String username) {
        boolean exists = userService.exists(username);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/exists")
    public ResponseEntity<Boolean> existAll(@RequestBody List<String> userNames) {

        boolean existAll = userService.existAll(userNames);

        return new ResponseEntity<>(existAll, HttpStatus.OK);
    }
}
