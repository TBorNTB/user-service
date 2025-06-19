package com.sejong.userservice.application.service;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.api.controller.dto.UserResponse;
import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public boolean joinProcess(JoinRequest joinRequest) {
        String username = joinRequest.getUsername();

        // 1. Check for existing username
        if (userRepository.existsByUsername(username)) {
            log.warn("Attempted to register with existing username: {}", username);
            return false; // Indicate failure
        }

        // 2. Build the User domain model from JoinRequest
        // Password encryption happens here.
        User user = User.builder()
                .username(username)
                .encryptPassword(bCryptPasswordEncoder.encode(joinRequest.getPassword()))
                .role("BASIC") // Default role for new users
                // Populate new fields from JoinRequest
                .realName(joinRequest.getRealName())
                .email(joinRequest.getEmail())
                .grade(joinRequest.getGrade())
                .major(joinRequest.getMajor())
                // Set creation/update timestamps (though they might be handled by JPA Auditing in UserEntity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 3. Save the User domain model
        try {
            userRepository.save(user);
            log.info("User registered successfully: {}", username);
            return true; // Indicate success
        } catch (Exception e) {
            log.error("Failed to save user {}: {}", username, e.getMessage());
            // More specific exception handling might be needed depending on repository errors
            return false; // Indicate failure
        }
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllUsers();

        return users.stream()
                .map(UserResponse::from) // UserResponse의 정적 팩토리 메서드 사용
                .collect(Collectors.toList());
    }
}
