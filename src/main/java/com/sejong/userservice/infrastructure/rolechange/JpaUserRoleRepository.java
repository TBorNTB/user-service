package com.sejong.userservice.infrastructure.rolechange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaUserRoleRepository extends JpaRepository<RoleChangeEntity, Long> {
    List<RoleChangeEntity> findAllByRequestStatus(RequestStatus requestStatus);
}
