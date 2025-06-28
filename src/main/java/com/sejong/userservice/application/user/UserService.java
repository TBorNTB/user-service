package com.sejong.userservice.application.user;

import com.sejong.userservice.application.exception.UserNotFoundException;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import com.sejong.userservice.core.token.RefreshTokenRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public JoinResponse joinProcess(JoinRequest joinRequest) {
        String username = joinRequest.getUsername();

        if (userRepository.existsByUsername(username)) {
            log.warn("Attempted to register with existing username: {}", username);
            throw new RuntimeException("이미 사용 중인 사용자 이름입니다: " + username);
        }

        User user = User.from(joinRequest, bCryptPasswordEncoder.encode(joinRequest.getPassword()));

        try {
            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", username);
            return JoinResponse.of(savedUser.getUsername(), "Registration successful!");
        } catch (Exception e) {
            log.error("Failed to save user {}: {}", username, e.getMessage());
            throw new RuntimeException("회원가입 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllUsers();

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest updateRequest) {
        User existingUser = userRepository.findByUsername(username);

        if (existingUser == null) {
            log.warn("User not found for update: {}", username);
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        existingUser.updateProfile(
                updateRequest
        );

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("User {} updated successfully.", username);
            return UserResponse.from(updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", username, e.getMessage());
            throw new RuntimeException("사용자 정보 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public UserResponse deleteUser(String username) {
        boolean exists = userRepository.existsByUsername(username);
        if (!exists) {
            log.warn("Attempted to delete non-existent user: {}", username);
            throw new UserNotFoundException("삭제하려는 사용자를 찾을 수 없습니다: " + username);
        }

        try {
            User user = userRepository.findByUsername(username);
            UserResponse userResponse = UserResponse.from(user);
            userRepository.deleteByUsername(username);
            refreshTokenRepository.revokeAllTokensForUser(username);
            log.info("User {} deleted successfully.", username);
            return userResponse;
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", username, e.getMessage());
            throw new RuntimeException("사용자 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public UserResponse logoutUser(String username) {
        try {
            refreshTokenRepository.revokeAllTokensForUser(username);
            User user = userRepository.findByUsername(username);
            log.info("User {} logged out successfully (all refresh tokens revoked).", username);
            return UserResponse.from(user);
        } catch (Exception e) {
            log.error("Failed to logout user {}: {}", username, e.getMessage());
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }
}
