package com.sejong.userservice.domain.user.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.SAME_WITH_PREVIOUS_PASSWORD;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.WRONG_PASSWORD;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.token.TokenService;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.LoginRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import com.sejong.userservice.domain.user.dto.response.JoinResponse;
import com.sejong.userservice.domain.user.dto.response.LoginResponse;
import com.sejong.userservice.domain.user.dto.response.UserNameInfo;
import com.sejong.userservice.domain.user.dto.response.UserRes;
import com.sejong.userservice.domain.user.dto.response.UserSearchResponse;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.pagination.CursorPageReq;
import com.sejong.userservice.support.common.pagination.CursorPageRes;
import com.sejong.userservice.support.common.pagination.SortDirection;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final AlarmService alarmService;
    private final JWTUtil jwtUtil;

    @Transactional
    public JoinResponse joinProcess(JoinRequest joinRequest) {
        User user = User.from(joinRequest, bCryptPasswordEncoder.encode(joinRequest.getPassword()));
        User savedUser = userRepository.save(user);
        savedUser.updateUsername();
        alarmService.save(AlarmDto.from(savedUser));
        return JoinResponse.of(savedUser.getNickname(), "Registration successful!");
    }

    @Transactional(readOnly = true)
    public Page<UserRes> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserRes::from);
    }

    @Transactional
    public UserRes updateUser(String username, UserUpdateRequest updateRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        user.updateProfile(updateRequest);
        return UserRes.from(user);
    }

    @Transactional
    public UserRes deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        UserRes userRes = UserRes.from(user);
        userRepository.deleteByUsername(username);
        log.info("User {} deleted successfully.", username);
        return userRes;
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        tokenService.blacklist(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username, List<String> collaboratorUsernames) {
        collaboratorUsernames.add(username);
        List<User> users = userRepository.findByUsernameIn(collaboratorUsernames);
        if (users.size() == collaboratorUsernames.size()) return true;
        return false;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new BaseException(NOT_FOUND_USER));

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
    public Map<String, UserNameInfo> getUserNameInfos(List<String> usernames) {
        List<User> users = userRepository.findByUsernameIn(usernames);

        return users.stream()
                .collect(Collectors.toMap(
                        User::getUsername,
                        user -> new UserNameInfo(user.getNickname(), user.getRealName())
                ));
    }


    @Transactional
    public String updateUserRole(Long id, UserRole newUserRole, String userRole) {
        if (!userRole.equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("어드민 전용 api입니다");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        user.updateUserRole(newUserRole);

        return "유저 업데이트 성공";
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public UserRes getUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        return UserRes.from(user);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
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
        return userRepository.findUserCount();
    }

    @Transactional(readOnly = true)
    public Page<UserRes> getUsersByRoles(List<UserRole> roles, String nickname, String realName, Pageable pageable) {
        List<UserRole> searchRoles = (roles == null || roles.isEmpty())
                ? List.of(UserRole.values())
                : roles;
        return userRepository.findByRolesAndSearch(searchRoles, nickname, realName, pageable).map(UserRes::from);
    }

    @Transactional(readOnly = true)
    public CursorPageRes<List<UserSearchResponse>> getAllUsersWithCursor(CursorPageReq cursorPageReq) {
        Long cursorId = cursorPageReq.getCursorId();
        Pageable pageable = PageRequest.of(0, cursorPageReq.getSize() + 1);

        List<User> users;
        if (cursorPageReq.getDirection() == SortDirection.ASC) {
            users = userRepository.findAllUsersWithCursorAsc(cursorId, pageable);
        } else {
            users = userRepository.findAllUsersWithCursor(cursorId, pageable);
        }

        List<UserSearchResponse> response = users.stream()
                .map(UserSearchResponse::from)
                .collect(Collectors.toList());

        return CursorPageRes.from(response, cursorPageReq.getSize(), UserSearchResponse::getId);
    }

    @Transactional(readOnly = true)
    public CursorPageRes<List<UserSearchResponse>> searchUsersByNicknameOrRealName(
            String nickname, String realName, CursorPageReq cursorPageReq) {
        Long cursorId = cursorPageReq.getCursorId();
        Pageable pageable = PageRequest.of(0, cursorPageReq.getSize() + 1);

        List<User> users;
        if (cursorPageReq.getDirection() == SortDirection.ASC) {
            users = userRepository.searchUsersByNicknameOrRealNameAsc(nickname, realName, cursorId, pageable);
        } else {
            users = userRepository.searchUsersByNicknameOrRealName(nickname, realName, cursorId, pageable);
        }

        List<UserSearchResponse> response = users.stream()
                .map(UserSearchResponse::from)
                .collect(Collectors.toList());

        return CursorPageRes.from(response, cursorPageReq.getSize(), UserSearchResponse::getId);
    }

    @Transactional(readOnly = true)
    public Long getNewUserCountSince(LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return userRepository.countByCreatedAtAfter(startDateTime);
    }
}
