package com.sejong.userservice.domain.rolechange;

import static com.sejong.userservice.common.exception.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.common.exception.ExceptionType.ROLE_CHANGE_NOT_FOUND;

import com.sejong.userservice.common.exception.BaseException;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChange;
import com.sejong.userservice.domain.rolechange.dto.request.RoleChangeRequest;
import com.sejong.userservice.domain.rolechange.dto.response.CreateRoleChange;
import com.sejong.userservice.domain.rolechange.dto.response.RoleChangeResponse;
import com.sejong.userservice.infrastructure.user.JpaUserRepository;
import com.sejong.userservice.infrastructure.user.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final JpaUserRepository jpaUserRepository;

    @Transactional(readOnly = true)
    public List<RoleChangeResponse> findAll() {
        return userRoleRepository.findAllByRequestStatus(RequestStatus.PENDING)
            .stream()
            .map(RoleChangeResponse::new)
            .toList();
    }

    @Transactional
    public String addRoleChange(String username, RoleChangeRequest request) {
        User user = userRepository.findByUsername(username);
        RoleChange roleChange = new CreateRoleChange(request.getRequestRole())
            .toRoleChangeEntity(user);
        userRoleRepository.save(roleChange);
        return "저장성공";
    }

    @Transactional
    public String manageRoleChange(boolean isAccepted, Long roleChangeId, String adminUsername) {
        RoleChange roleChange = userRoleRepository.findById(roleChangeId)
            .orElseThrow(() -> new BaseException(ROLE_CHANGE_NOT_FOUND));
        if (isAccepted) {
            roleChange.updateAccept(adminUsername);
            UserEntity user = jpaUserRepository.findByEmail(roleChange.getEmail()).orElseThrow(() -> new BaseException(NOT_FOUND_USER));
            user.updateUserRole(roleChange.getRequestedRole());
            return "승인 성공";
        }

        roleChange.updateReject(adminUsername);
        return "승인 거절";
    }
}
