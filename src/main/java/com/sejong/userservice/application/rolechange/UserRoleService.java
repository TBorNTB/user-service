package com.sejong.userservice.application.rolechange;

import com.sejong.userservice.core.user.*;
import com.sejong.userservice.infrastructure.rolechange.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleChange> findAll() {
        return userRoleRepository.findAllAboutStatusIsPending();
    }

    @Transactional
    public String addRoleChange(String username, String requestRole) {
        User user = userRepository.findByUsername(username);
        RoleChange roleChange = RoleChange.from(user, requestRole, RequestStatus.PENDING);
        RoleChange savedRoleChange = userRoleRepository.save(roleChange);
        return "저장성공";
    }

    @Transactional
    public String manageRoleChange(boolean isAccepted, Long roleChangeId, String adminUsername) {

        if(isAccepted) {
            userRoleRepository.updateAccept(roleChangeId,adminUsername);
            return "승인 성공";
        }
        else{
            userRoleRepository.updateReject(roleChangeId, adminUsername);
            return "승인 거절";
        }
    }
}
