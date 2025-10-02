package com.sejong.userservice.infrastructure.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRoleRepository extends JpaRepository<RoleChangeEntity,Long> {
}
