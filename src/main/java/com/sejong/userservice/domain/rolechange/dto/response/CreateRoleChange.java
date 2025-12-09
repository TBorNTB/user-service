package com.sejong.userservice.domain.rolechange.dto.response;

import com.sejong.userservice.core.user.User;
import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChangeEntity;
import com.sejong.userservice.domain.rolechange.domain.UserRole;

public record CreateRoleChange(
        UserRole roleChange
) {
    public RoleChangeEntity toRoleChangeEntity(User user) {
        return RoleChangeEntity.from(
            user,
            roleChange,
            RequestStatus.PENDING
        );
    }
}
