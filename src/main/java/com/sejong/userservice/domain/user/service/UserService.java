package com.sejong.userservice.domain.user.service;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.SAME_WITH_PREVIOUS_PASSWORD;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.WRONG_PASSWORD;

import com.sejong.userservice.client.file.FileUploader;
import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.domain.comment.repository.CommentRepository;
import com.sejong.userservice.domain.like.domain.Like;
import com.sejong.userservice.domain.like.repository.LikeRepository;
import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.token.TokenService;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.dto.request.JoinRequest;
import com.sejong.userservice.domain.user.dto.request.LoginRequest;
import com.sejong.userservice.domain.user.dto.request.UserUpdateRequest;
import com.sejong.userservice.domain.user.dto.response.JoinResponse;
import com.sejong.userservice.domain.user.dto.response.LikedPostResponse;
import com.sejong.userservice.domain.user.dto.response.LoginResponse;
import com.sejong.userservice.domain.user.dto.response.UserActivityStatsResponse;
import com.sejong.userservice.domain.user.dto.response.UserCommentPostResponse;
import com.sejong.userservice.domain.user.dto.response.UserNameInfo;
import com.sejong.userservice.domain.user.dto.response.UserRes;
import com.sejong.userservice.domain.user.dto.response.UserRoleCountResponse;
import com.sejong.userservice.domain.user.dto.response.UserSearchResponse;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.domain.view.repository.ViewJPARepository;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;
import com.sejong.userservice.support.common.internal.PostInternalFacade;
import com.sejong.userservice.support.common.pagination.CursorPageReq;
import com.sejong.userservice.support.common.pagination.CursorPageRes;
import com.sejong.userservice.support.common.pagination.SortDirection;
import com.sejong.userservice.support.common.redis.RedisKeyUtil;
import com.sejong.userservice.support.common.redis.RedisService;
import com.sejong.userservice.support.common.security.jwt.JWTUtil;
import com.sejong.userservice.support.common.util.FileValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final RedisService redisService;
    private final LikeRepository likeRepository;
    private final ViewJPARepository viewJPARepository;
    private final AlarmService alarmService;
    private final CommentRepository commentRepository;
    private final PostInternalFacade postInternalFacade;

    private final FileUploader fileUploader;
    private final FileValidator fileValidator;

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
        return userRepository.findAll(pageable).map(this::toUserResWithUrl);
    }

    @Transactional
    public UserRes updateUser(String username, UserUpdateRequest updateRequest) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        user.updateProfile(updateRequest);
        return toUserResWithUrl(user);
    }

    @Transactional
    public UserRes deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        UserRes userRes = toUserResWithUrl(user);
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

        return LoginResponse.from(user, "로그인 성공", accessToken, refreshToken);
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

    /**
     * 여러 유저의 등급을 일괄 변경합니다. 단일 유저도 배열에 1개만 넣어서 사용 가능합니다.
     */
    @Transactional
    public String batchUpdateUserRole(List<String> usernames, UserRole newUserRole) {
        List<User> users = userRepository.findByUsernameIn(usernames);
        
        if (users.size() != usernames.size()) {
            throw new BaseException(NOT_FOUND_USER);
        }

        users.forEach(user -> user.updateUserRole(newUserRole));
        
        if (users.size() == 1) {
            return "유저 등급 변경 성공";
        }
        return String.format("%d명의 유저 등급 변경 성공", users.size());
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public UserRes getUserInfo(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        return toUserResWithUrl(user);
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
        return userRepository.findByRolesAndSearch(searchRoles, nickname, realName, pageable).map(this::toUserResWithUrl);
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
                .map(this::toUserSearchResponseWithUrl)
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
                .map(this::toUserSearchResponseWithUrl)
                .collect(Collectors.toList());

        return CursorPageRes.from(response, cursorPageReq.getSize(), UserSearchResponse::getId);
    }

    @Transactional(readOnly = true)
    public Long getNewUserCountSince(LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return userRepository.countByCreatedAtAfter(startDateTime);
    }


    @Transactional(readOnly = true)
    public UserRoleCountResponse getUserRoleCounts() {
        Long guestCount = userRepository.countByRole(UserRole.GUEST);
        Long associateMemberCount = userRepository.countByRole(UserRole.ASSOCIATE_MEMBER);
        Long fullMemberCount = userRepository.countByRole(UserRole.FULL_MEMBER);
        Long seniorCount = userRepository.countByRole(UserRole.SENIOR);
        Long adminCount = userRepository.countByRole(UserRole.ADMIN);
        Long totalCount = userRepository.findUserCount();

        return UserRoleCountResponse.builder()
                .guestCount(guestCount)
                .associateMemberCount(associateMemberCount)
                .fullMemberCount(fullMemberCount)
                .seniorCount(seniorCount)
                .adminCount(adminCount)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 사용자의 활동 통계를 조회합니다 (작성한 글 개수, 총 조회수, 받은 좋아요, 받은 댓글).
     */
    @Transactional(readOnly = true)
    public UserActivityStatsResponse getUserActivityStats(String username) {
        // 사용자가 작성한 글 목록 가져오기
        Map<PostType, List<Long>> userPostIds = postInternalFacade.getUserPostIds(username);

        Long totalPostCount = 0L;
        Long totalViewCount = 0L;
        Long totalLikeCount = 0L;
        Long totalCommentCount = 0L;

        // 각 PostType별로 통계 계산
        for (PostType postType : PostType.values()) {
            List<Long> postIds = userPostIds.getOrDefault(postType, List.of());
            
            if (postIds.isEmpty()) {
                continue;
            }

            // 작성한 글 개수 계산
            totalPostCount += postIds.size();

            // 총 조회수 계산 (Redis에서 가져오기)
            totalViewCount += calculateTotalViewCountFromRedis(postType, postIds);
            
            // 받은 좋아요 수 계산
            totalLikeCount += likeRepository.countByPostTypeAndPostIds(postType, postIds);
            
            // 받은 댓글 수 계산
            totalCommentCount += commentRepository.countByPostTypeAndPostIds(postType, postIds);
        }

        return UserActivityStatsResponse.builder()
                .totalPostCount(totalPostCount)
                .totalViewCount(totalViewCount)
                .totalLikeCount(totalLikeCount)
                .totalCommentCount(totalCommentCount)
                .build();
    }

    /**
     * Redis에서 특정 글들의 조회수 합계를 계산합니다.
     */
    private Long calculateTotalViewCountFromRedis(PostType postType, List<Long> postIds) {
        long sum = 0L;
        for (Long postId : postIds) {
            String viewCountKey = RedisKeyUtil.viewCountKey(postType, postId);
            Long viewCount = getViewCountFromRedisOrDB(postType, postId, viewCountKey);
            sum += viewCount;
        }
        return sum;
    }

    /**
     * Redis에서 조회수를 가져오고, 없으면 DB에서 가져옵니다.
     */
    private Long getViewCountFromRedisOrDB(PostType postType, Long postId, String viewCountKey) {
        if (redisService.hasKey(viewCountKey)) {
            return redisService.getCount(viewCountKey);
        }

        // Redis에 없으면 DB에서 조회
        return viewJPARepository.findByPostTypeAndPostId(postType, postId)
                .map(com.sejong.userservice.domain.view.domain.View::getViewCount)
                .orElse(0L);
    }

    /**
     * 사용자가 좋아요 누른 글의 postId와 postType 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<LikedPostResponse> getLikedPosts(String username, Pageable pageable) {
        Page<Like> likes = likeRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
        return likes.map(like -> LikedPostResponse.builder()
                .postType(like.getPostType())
                .postId(like.getPostId())
                .build());
    }

    /**
     * 사용자가 작성한 댓글이 있는 글의 postId와 postType 목록을 조회합니다.
     * 중복 제거하여 각 글당 하나의 레코드만 반환합니다.
     */
    @Transactional(readOnly = true)
    public Page<UserCommentPostResponse> getCommentedPosts(String username, Pageable pageable) {
        Page<com.sejong.userservice.domain.comment.domain.Comment> comments = 
                commentRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
        
        // postType과 postId로 그룹화하여 중복 제거
        Map<String, com.sejong.userservice.domain.comment.domain.Comment> uniquePosts = comments.stream()
                .collect(Collectors.toMap(
                        comment -> comment.getPostType().name() + "_" + comment.getPostId(),
                        comment -> comment,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));
        
        // Map을 List로 변환하고 정렬 (최신순)
        List<UserCommentPostResponse> uniquePostList = uniquePosts.values().stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .map(comment -> UserCommentPostResponse.builder()
                        .postType(comment.getPostType())
                        .postId(comment.getPostId())
                        .build())
                .collect(Collectors.toList());
        
        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniquePostList.size());
        List<UserCommentPostResponse> pagedList = start < uniquePostList.size() 
                ? uniquePostList.subList(start, end) 
                : List.of();
        
        return new org.springframework.data.domain.PageImpl<>(
                pagedList, 
                pageable, 
                uniquePostList.size()
        );
    }

    @Transactional
    public UserRes updateProfileImage(String username, MultipartFile imageFile) {
        fileValidator.validateImageFile(imageFile);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        if (user.getProfileImageKey() != null) {
            try {
                fileUploader.delete(user.getProfileImageKey());
                log.info("기존 프로필 이미지 삭제: {}", user.getProfileImageKey());
            } catch (Exception e) {
                log.warn("기존 이미지 삭제 실패 (계속 진행): {}", user.getProfileImageKey(), e);
            }
        }
        String directory = String.format("%s/users/%d/profile", "user-service", user.getId());

        String key = fileUploader.upㅇloadFile(
            imageFile,
            directory,
            imageFile.getOriginalFilename()
        );

        user.updateProfileImage(key);
        log.info("프로필 이미지 업데이트 완료: userId={}, key={}", user.getId(), key);

        return toUserResWithUrl(user);
    }

    /**
     * User -> UserRes 변환 시 profileImageKey를 URL로 변환
     * - 외부 URL (GitHub 등): 그대로 반환
     * - 내부 key: endpoint/bucket/key 형태로 조립
     */
    private UserRes toUserResWithUrl(User user) {
        UserRes userRes = UserRes.from(user);
        if (user.getProfileImageKey() != null) {
            userRes.setProfileImageUrl(fileUploader.getFileUrl(user.getProfileImageKey()));
        }
        return userRes;
    }

    /**
     * User -> UserSearchResponse 변환 시 profileImageKey를 URL로 변환
     */
    private UserSearchResponse toUserSearchResponseWithUrl(User user) {
        UserSearchResponse response = UserSearchResponse.from(user);
        if (user.getProfileImageKey() != null) {
            response.setProfileImageUrl(fileUploader.getFileUrl(user.getProfileImageKey()));
        }
        return response;
    }
}
