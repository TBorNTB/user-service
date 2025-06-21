package com.sejong.userservice.application.service;

import com.sejong.userservice.api.controller.dto.JoinRequest;
import com.sejong.userservice.api.controller.dto.UserResponse;
import com.sejong.userservice.api.controller.dto.UserUpdateRequest;
import com.sejong.userservice.domain.model.User;
import com.sejong.userservice.domain.repository.RefreshTokenRepository;
import com.sejong.userservice.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
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

    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest updateRequest) {
        User existingUser = userRepository.findByUsername(username);

        if (existingUser == null) {
            log.warn("User not found for update: {}", username);
            return null;
        }

        existingUser.updateProfile(
                updateRequest.getRealName(),
                updateRequest.getEmail(),
                updateRequest.getGrade(),
                updateRequest.getMajor()
        );

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("User {} updated successfully.", username);
            return UserResponse.from(updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * 사용자 정보를 삭제하는 메서드
     * @param username 삭제할 사용자의 식별자 (username)
     * @return 삭제 성공 시 true, 사용자 없음 또는 삭제 실패 시 false
     */
    @Transactional
    public boolean deleteUser(String username) {
        // 1. 사용자 존재 여부 확인
        boolean exists = userRepository.existsByUsername(username);
        if (!exists) {
            log.warn("Attempted to delete non-existent user: {}", username);
            return false; // 사용자가 없으면 실패
        }

        // 2. 사용자 삭제
        try {
            userRepository.deleteByUsername(username);
            refreshTokenRepository.revokeAllTokensForUser(username); // 사용자 삭제 시 관련 리프레시 토큰도 무효화
            log.info("User {} deleted successfully.", username);
            return true; // 삭제 성공
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", username, e.getMessage());
            // 데이터베이스 제약 조건 위반 등의 예외 처리
            return false; // 삭제 실패
        }
    }

    /**
     * 사용자 로그아웃 처리
     * @param username 로그아웃할 사용자의 식별자
     * @return 로그아웃 성공 시 true
     */
    @Transactional
    public boolean logoutUser(String username) {
        try {
            refreshTokenRepository.revokeAllTokensForUser(username); // 해당 사용자의 모든 리프레시 토큰 무효화
            log.info("User {} logged out successfully (all refresh tokens revoked).", username);
            return true;
        } catch (Exception e) {
            log.error("Failed to logout user {}: {}", username, e.getMessage());
            return false;
        }
    }
}
