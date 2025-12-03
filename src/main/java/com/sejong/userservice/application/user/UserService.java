package com.sejong.userservice.application.user;

import static com.sejong.userservice.application.common.exception.ExceptionType.SAME_WITH_PREVIOUS_PASSWORD;

import com.sejong.userservice.application.common.exception.BaseException;
import com.sejong.userservice.application.token.TokenService;
import com.sejong.userservice.application.user.dto.*;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.core.user.UserRole;
import com.sejong.userservice.infrastructure.alarm.kafka.publisher.EventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;

    @Transactional
    public JoinResponse joinProcess(JoinRequest joinRequest) {
        String nickname = joinRequest.getNickname();

        // todo: email로 변경
        if (userRepository.existsByNickname(nickname)) {
            log.warn("Attempted to register with existing nickname: {}", nickname);
            // todo: BaseException
            throw new RuntimeException("이미 사용 중인 사용자 닉네임입니다: " + nickname);
        }

        User user = User.from(joinRequest, bCryptPasswordEncoder.encode(joinRequest.getPassword()));

        try {
            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", nickname);
            eventPublisher.publishSignUpAlarm(savedUser);
            return JoinResponse.of(savedUser.getNickname(), "Registration successful!");
        } catch (Exception e) {
            log.error("Failed to save user {}: {}", nickname, e.getMessage());
            throw new RuntimeException("회원가입 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllUsers();

        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPageNationResponse getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAllUsers(pageable);

        return UserPageNationResponse.from(userPage);
    }

    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest updateRequest) {
        User existingUser = userRepository.findByUsername(username);
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
        try {
            User user = userRepository.findByUsername(username);
            UserResponse userResponse = UserResponse.from(user);
            userRepository.deleteByUsername(username);
            // 토큰 처리는 TokenService에서 관리
            log.info("User {} deleted successfully.", username);
            return userResponse;
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", username, e.getMessage());
            throw new RuntimeException("사용자 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        try {
            tokenService.blacklist(accessToken, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public UserResponse grantAdminRole(String targetUsername) {
        User userToGrantAdmin = userRepository.findByUsername(targetUsername);

        if (userToGrantAdmin.getRole() == UserRole.ADMIN) {
            log.info("User {} already has ADMIN role. No change made.", targetUsername);
            return UserResponse.from(userToGrantAdmin);
        }

        userToGrantAdmin.getRole(UserRole.ADMIN);

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
    public UserResponse confirmMember(String targetUsername, Integer generation) {
        User userToApprove = userRepository.findByUsername(targetUsername);

        if (userToApprove.getRole() != UserRole.GUEST) {
            log.warn("User {} is not in UNCONFIRMED_MEMBER state. Current role: {}", targetUsername,
                    userToApprove.getRole());
            throw new IllegalStateException(
                    "사용자 " + targetUsername + "은(는) 승인 대기 상태가 아닙니다. (현재 권한: " + userToApprove.getRole().name() + ")");
        }

        userToApprove.approveAs(UserRole.ASSOCIATE_MEMBER, generation);

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
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username, List<String> collaboratorUsernames) {
        return userRepository.existsByUsernames(username, collaboratorUsernames);
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

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error finding user by email {}: {}", email, e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllUsernames(List<String> usernames) {
        List<User> users = userRepository.findByUsernameIn(usernames);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getUsername,
                        User::getNickname
                ));
    }


    @Transactional
    public String updateUserRole(Long id, String newUserRole, String userRole) {
        if (!userRole.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("어드민 전용 api입니다");
        }

        userRepository.updateUserRole(id, newUserRole);
        return "유저 업데이트 성공";
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String username) {
        User user = userRepository.getUserInfo(username);
        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        // 이전 비밀번호와 동일한지 확인 (BCrypt matches 사용)
        if (bCryptPasswordEncoder.matches(newPassword, user.getEncryptPassword())) {
            throw new BaseException(SAME_WITH_PREVIOUS_PASSWORD);
        }
        String encryptedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        user.updatePassword(encryptedNewPassword);
        userRepository.save(user);
    }


}
