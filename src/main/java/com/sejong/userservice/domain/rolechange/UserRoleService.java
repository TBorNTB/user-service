package com.sejong.userservice.domain.rolechange;

import com.sejong.userservice.core.user.User;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChangeEntity;
import com.sejong.userservice.domain.rolechange.dto.request.RoleChangeRequest;
import com.sejong.userservice.domain.rolechange.dto.response.CreateRoleChange;
import com.sejong.userservice.domain.rolechange.dto.response.RoleChangeResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final JpaUserRoleRepository userRoleRepository;
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
        User user = userRepository.findByUsername(username);
        RoleChangeEntity roleChange = new CreateRoleChange(request.getRequestRole())
            .toRoleChangeEntity(user);
        RoleChangeEntity savedRoleChange = userRoleRepository.save(roleChange);
        return "저장성공";
    }

    @Transactional
    public String manageRoleChange(boolean isAccepted, Long roleChangeId, String adminUsername) {

        if (isAccepted) {
            RoleChangeEntity roleChangeEntity = userRoleRepository.findById(roleChangeId)
                .orElseThrow(() -> new RuntimeException("해당 roleChange는 존재하지 않습니다."));
            roleChangeEntity.updateAccept(adminUsername);
            return "승인 성공";
        } else {
            RoleChangeEntity roleChangeEntity = userRoleRepository.findById(roleChangeId)
                .orElseThrow(() -> new RuntimeException("해당 roleChange는 존재하지 않습니다."));
            roleChangeEntity.updateReject(adminUsername);
            return "승인 거절";
        }
    }
}
