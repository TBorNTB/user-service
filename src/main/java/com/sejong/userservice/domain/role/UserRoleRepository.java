package com.sejong.userservice.domain.role;

import com.sejong.userservice.domain.role.domain.RequestStatus;
import com.sejong.userservice.domain.role.domain.RoleChange;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<RoleChange, Long> {
    List<RoleChange> findAllByRequestStatus(RequestStatus requestStatus);
}
