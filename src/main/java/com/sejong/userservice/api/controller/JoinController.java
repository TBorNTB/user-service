package com.sejong.userservice.api.controller;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.application.service.JoinService;
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
    public String joinProcess(@RequestBody JoinRequest joinRequest) {
        joinService.joinProcess(joinRequest);

        return "메롱";
    }
}
