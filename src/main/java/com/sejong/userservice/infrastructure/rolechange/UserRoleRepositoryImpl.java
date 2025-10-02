package com.sejong.userservice.infrastructure.rolechange;

import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {
    private final JpaUserRoleRepository userRoleRepository;

    @Override
    public List<RoleChange> findAllAboutStatusIsPending() {
        return userRoleRepository.findAllByRequestStatus(RequestStatus.PENDING).stream()
                .map(RoleChangeEntity::toDomain)
                .toList();
    }

    @Override
    public RoleChange save(RoleChange roleChange) {
        RoleChangeEntity roleChangeEntity = RoleChangeEntity.from(roleChange);
        RoleChangeEntity savedRoleChangeEntity = userRoleRepository.save(roleChangeEntity);
        return savedRoleChangeEntity.toDomain();
    }

    @Override
    public void updateAccept(Long roleChangeId, String adminUsername) {
        RoleChangeEntity roleChangeEntity = userRoleRepository.findById(roleChangeId)
                .orElseThrow(() -> new RuntimeException("해당 roleChange는 존재하지 않습니다."));
        roleChangeEntity.updateAccept(adminUsername);
    }

    @Override
    public void updateReject(Long roleChangeId, String adminUsername) {
        RoleChangeEntity roleChangeEntity = userRoleRepository.findById(roleChangeId)
                .orElseThrow(() -> new RuntimeException("해당 roleChange는 존재하지 않습니다."));
        roleChangeEntity.updateReject(adminUsername);
    }

}
