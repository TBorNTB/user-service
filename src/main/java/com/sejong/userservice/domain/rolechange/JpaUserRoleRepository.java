package com.sejong.userservice.domain.rolechange;

import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChangeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRoleRepository extends JpaRepository<RoleChangeEntity, Long> {
    List<RoleChangeEntity> findAllByRequestStatus(RequestStatus requestStatus);
}
