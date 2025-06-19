package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.UserInfosResponse;
import com.sejong.userservice.application.service.UserService;
import com.sejong.userservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

    @GetMapping
    public String userController() {
        return "메롱";
    }


}
