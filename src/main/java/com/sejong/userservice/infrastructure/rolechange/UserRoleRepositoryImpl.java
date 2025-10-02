package com.sejong.userservice.infrastructure.user;

import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.UserRole;
import com.sejong.userservice.core.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {
    private final JpaUserRoleRepository userRoleRepository;


    @Override
    public List<RoleChange> findAll() {
        return userRoleRepository.findAll().stream()
                .map(RoleChangeEntity::toDomain)
                .toList();
    }
}
