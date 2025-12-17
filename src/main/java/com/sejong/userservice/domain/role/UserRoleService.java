package com.sejong.userservice.domain.role;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.NOT_FOUND_USER;
import static com.sejong.userservice.support.common.exception.type.ExceptionType.ROLE_CHANGE_NOT_FOUND;

import com.sejong.userservice.domain.role.domain.RequestStatus;
import com.sejong.userservice.domain.role.domain.RoleChange;
import com.sejong.userservice.domain.role.dto.request.RoleChangeRequest;
import com.sejong.userservice.domain.role.dto.response.CreateRoleChange;
import com.sejong.userservice.domain.role.dto.response.RoleChangeResponse;
import com.sejong.userservice.domain.user.domain.User;
import com.sejong.userservice.domain.user.repository.UserRepository;
import com.sejong.userservice.support.common.exception.type.BaseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleChangeResponse> findAll() {
        return userRoleRepository.findAllByRequestStatus(RequestStatus.PENDING)
                .stream()
                .map(RoleChangeResponse::new)
                .toList();
    }

    @Transactional
    public String addRoleChange(String username, RoleChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
        RoleChange roleChange = new CreateRoleChange(request.getRequestRole()).toRoleChangeEntity(user);
        userRoleRepository.save(roleChange);
        return "저장성공";
    }

    @Transactional
    public String manageRoleChange(boolean isAccepted, Long roleChangeId, String adminUsername) {
        RoleChange roleChange = userRoleRepository.findById(roleChangeId)
                .orElseThrow(() -> new BaseException(ROLE_CHANGE_NOT_FOUND));
        if (isAccepted) {
            roleChange.updateAccept(adminUsername);
            User user = userRepository.findByEmail(roleChange.getEmail())
                    .orElseThrow(() -> new BaseException(NOT_FOUND_USER));
            user.updateUserRole(roleChange.getRequestedRole());
            return "승인 성공";
        }

        roleChange.updateReject(adminUsername);
        return "승인 거절";
    }
}
