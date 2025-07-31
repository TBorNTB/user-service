package com.sejong.userservice.application.user;

import static com.sejong.userservice.application.exception.exception.ExceptionType.DUPLICATED_USER_REQUEST;
import static com.sejong.userservice.application.exception.exception.ExceptionType.EMPTY_USER_REQUEST;
import static com.sejong.userservice.application.exception.exception.ExceptionType.NOT_FOUND_USER;

import com.sejong.userservice.application.exception.UserNotFoundException;
import com.sejong.userservice.application.exception.exception.ApiException;
import com.sejong.userservice.application.user.dto.JoinRequest;
import com.sejong.userservice.application.user.dto.JoinResponse;
import com.sejong.userservice.application.user.dto.UserResponse;
import com.sejong.userservice.application.user.dto.UserUpdateRequest;
import com.sejong.userservice.core.token.TokenRepository;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.core.user.UserRole;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final TokenRepository tokenRepository;

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
            tokenRepository.revokeAllTokensForUser(username);
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
            tokenRepository.revokeAllTokensForUser(username);
            log.info("User {} logged out successfully (all refresh tokens revoked).", username);
            User user = userRepository.findByUsername(username);
            return UserResponse.from(user);
        } catch (Exception e) {
            log.error("Failed to logout user {}: {}", username, e.getMessage());
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public UserResponse grantAdminRole(String targetUsername) {
        User userToGrantAdmin = userRepository.findByUsername(targetUsername);

        if (userToGrantAdmin == null) {
            log.warn("Attempted to grant admin role to non-existent user: {}", targetUsername);
            throw new UserNotFoundException("관리자 권한을 부여할 사용자를 찾을 수 없습니다: " + targetUsername);
        }

        if (userToGrantAdmin.getRole() == UserRole.ADMIN) {
            log.info("User {} already has ADMIN role. No change made.", targetUsername);
            return UserResponse.from(userToGrantAdmin);
        }

        userToGrantAdmin.grantRole(UserRole.ADMIN);

        try {
            User updatedUser = userRepository.save(userToGrantAdmin);
            log.info("Admin role granted successfully to user: {}", targetUsername);
            return UserResponse.from(updatedUser);
        } catch (Exception e) {
            log.error("Failed to grant admin role to user {}: {}", targetUsername, e.getMessage());
            throw new RuntimeException("관리자 권한 부여 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public UserResponse confirmMember(String targetUsername) {
        User userToApprove = userRepository.findByUsername(targetUsername);

        if (userToApprove == null) {
            log.warn("Attempted to approve non-existent user: {}", targetUsername);
            throw new UserNotFoundException("승인할 사용자를 찾을 수 없습니다: " + targetUsername);
        }

        if (userToApprove.getRole() != UserRole.UNCONFIRMED_MEMBER) {
            log.warn("User {} is not in UNCONFIRMED_MEMBER state. Current role: {}", targetUsername,
                    userToApprove.getRole());
            throw new IllegalStateException(
                    "사용자 " + targetUsername + "은(는) 승인 대기 상태가 아닙니다. (현재 권한: " + userToApprove.getRole().name() + ")");
        }

        userToApprove.grantRole(UserRole.MEMBER);

        try {
            User updatedUser = userRepository.save(userToApprove);
            log.info("User {} membership approved successfully. New role: {}", targetUsername, updatedUser.getRole());
            return UserResponse.from(updatedUser);
        } catch (Exception e) {
            log.error("Failed to approve membership for user {}: {}", targetUsername, e.getMessage());
            throw new RuntimeException("사용자 회원 승인 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(String userId) {
        return userRepository.existsByUsername(userId);
    }

    @Transactional(readOnly = true)
    public boolean existAll(List<String> userIds) {
        Set<String> usernames = new HashSet<>(userIds);
        if (usernames.isEmpty()) {
            log.warn("사용자 ID 목록이 비어 있습니다.");
            return false;
        }
        if (usernames.size() != userIds.size()) {
            log.warn("사용자 ID 목록에 중복된 값이 있습니다.: {}", userIds);
            return false;
        }
        List<User> users = userRepository.findAllByUsernameIn(userIds);
        if (users.size() != userIds.size()) {
            log.warn("Some users not found for IDs: {}", userIds);
            return false;
        }
        return true;
    }

}
