package com.sejong.userservice.domain.user.service;

import static com.sejong.userservice.support.common.exception.ExceptionType.SAME_WITH_PREVIOUS_PASSWORD;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.token.TokenService;
import com.sejong.userservice.domain.user.JpaUserRepository;
import com.sejong.userservice.domain.user.User;
import com.sejong.userservice.domain.user.UserRepository;
import com.sejong.userservice.domain.user.dto.JoinRequest;
import com.sejong.userservice.domain.user.dto.JoinResponse;
import com.sejong.userservice.domain.user.dto.UserPageNationResponse;
import com.sejong.userservice.domain.user.dto.UserResponse;
import com.sejong.userservice.domain.user.dto.UserUpdateRequest;
import com.sejong.userservice.support.common.exception.BaseException;
import java.util.List;
import java.util.Map;
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
    private final JpaUserRepository jpaUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;
    private final AlarmService alarmService;

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
            alarmService.save(AlarmDto.from(savedUser));
            log.info("SignUp alarm is made successfully: {}", nickname);
            return JoinResponse.of(savedUser.getNickname(), "Registration successful!");
        } catch (Exception e) {
            log.error("Failed to save user {}: {}", nickname, e.getMessage());
            throw new RuntimeException("회원가입 중 데이터베이스 오류가 발생했습니다.", e);
        }
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

    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username, List<String> collaboratorUsernames) {
        return userRepository.existsByUsernames(username, collaboratorUsernames);
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
        List<User> users = userRepository.findUsernamesIn(usernames);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getUsername,
                        User::getNickname
                ));
    }


    @Transactional
    public String updateUserRole(Long id, UserRole newUserRole, String userRole) {
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
        User user = userRepository.findByUsername(username);
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

    @Transactional(readOnly = true)
    public Long getUserCount() {
        return userRepository.findUsersCount();
    }
}
