package com.sejong.userservice.application.service;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JoinService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinRequest joinRequest) {
        String username = joinRequest.getUsername();
        String password = joinRequest.getPassword();

        boolean exists = userRepository.existsByUsername(username);

        if (exists) {
            return;
        }

        User user = User.builder()
                .username(username)
                .encryptPassword(bCryptPasswordEncoder.encode(password))
                .role("BASIC")
                .build();

        userRepository.save(user);
    }
}
