package com.sejong.userservice.application.user;

import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.UserRepository;
import com.sejong.userservice.core.user.UserRole;
import com.sejong.userservice.core.user.UserRoleRepository;
import com.sejong.userservice.infrastructure.user.RoleChangeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    public List<RoleChange> findAll() {
        return userRoleRepository.findAll();
    }
}
