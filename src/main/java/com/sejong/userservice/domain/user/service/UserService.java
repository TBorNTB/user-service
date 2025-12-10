package com.sejong.userservice.domain.user.service;

import static com.sejong.userservice.support.common.exception.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.support.common.exception.ExceptionType.SAME_WITH_PREVIOUS_PASSWORD;
import static com.sejong.userservice.support.common.exception.ExceptionType.WRONG_PASSWORD;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.token.TokenService;
import com.sejong.userservice.domain.user.JpaUserRepository;
import com.sejong.userservice.domain.user.domain.UserEntity;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.LoginRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import com.sejong.userservice.domain.user.dto.response.JoinResponse;
import com.sejong.userservice.domain.user.dto.response.LoginResponse;
import com.sejong.userservice.domain.user.dto.response.UserRes;
import com.sejong.userservice.support.common.exception.BaseException;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    private final JpaUserRepository jpaUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;
    private final AlarmService alarmService;
    private final JWTUtil jwtUtil;

    @Transactional
    public JoinResponse joinProcess(JoinRequest joinRequest) {
        UserEntity user = UserEntity.from(joinRequest, bCryptPasswordEncoder.encode(joinRequest.getPassword()));
        UserEntity savedUser = jpaUserRepository.save(user);
        savedUser.updateUsername();
        alarmService.save(AlarmDto.from(savedUser));
        return JoinResponse.of(savedUser.getNickname(), "Registration successful!");
    }

    @Transactional(readOnly = true)
    public Page<UserRes> getAllUsers(Pageable pageable) {
        return jpaUserRepository.findAll(pageable).map(UserRes::from);
    }

    @Transactional
    public UserRes updateUser(String username, UserUpdateRequest updateRequest) {
        UserEntity user = jpaUserRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        user.updateProfile(updateRequest);
        return UserRes.from(user);
    }

    @Transactional
    public UserRes deleteUser(String username) {
        UserEntity user = jpaUserRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        UserRes userRes = UserRes.from(user);
        jpaUserRepository.deleteByUsername(username);
        log.info("User {} deleted successfully.", username);
        return userRes;
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenService.blacklist(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username, List<String> collaboratorUsernames) {
        collaboratorUsernames.add(username);
        List<UserEntity> users = jpaUserRepository.findByUsernameIn(collaboratorUsernames);
        if (users.size() == collaboratorUsernames.size()) return true;
        return false;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        UserEntity user = jpaUserRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        // 비밀번호 검증
        if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getEncryptPassword())) {
            throw new BaseException(WRONG_PASSWORD);
        }

        String username = user.getUsername();
        String accessToken = jwtUtil.createAccessToken(username, user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(username);

        // Access Token을 쿠키에 저장 (API Gateway에서 읽기 위해)
        Cookie accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);

        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = jwtUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        return new LoginResponse("로그인 성공", accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllUsernames(List<String> usernames) {
        List<UserEntity> users = jpaUserRepository.findByUsernameIn(usernames);

        return users.stream()
                .collect(Collectors.toMap(
                        UserEntity::getUsername,
                        UserEntity::getNickname
                ));
    }


    @Transactional
    public String updateUserRole(Long id, UserRole newUserRole, String userRole) {
        if (!userRole.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("어드민 전용 api입니다");
        }

        UserEntity userEntity = jpaUserRepository.findById(id).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        userEntity.updateUserRole(newUserRole);

        return "유저 업데이트 성공";
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(String username) {
        return jpaUserRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public UserRes getUserInfo(String username) {
        UserEntity user = jpaUserRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        return UserRes.from(user);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        UserEntity user = jpaUserRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        // 이전 비밀번호와 동일한지 확인 (BCrypt matches 사용)
        if (bCryptPasswordEncoder.matches(newPassword, user.getEncryptPassword())) {
            throw new BaseException(SAME_WITH_PREVIOUS_PASSWORD);
        }
        String encryptedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        user.updatePassword(encryptedNewPassword);
        jpaUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Long getUserCount() {
        return jpaUserRepository.findUserCount();
    }
}
