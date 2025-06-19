package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.application.service.JoinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinProcess(@Valid @RequestBody JoinRequest joinRequest) {
        boolean success = joinService.joinProcess(joinRequest);

        if (success) {
            return new ResponseEntity<>("Registration successful!", HttpStatus.CREATED); // 201 Created
        } else {
            // Handle specific error cases here, e.g., username already exists
            return new ResponseEntity<>("Registration failed: Username already exists or invalid data.", HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }
}
