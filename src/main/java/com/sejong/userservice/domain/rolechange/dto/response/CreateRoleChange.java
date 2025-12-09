package com.sejong.userservice.domain.rolechange.dto.response;

import com.sejong.userservice.core.user.User;
import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChange;
import com.sejong.userservice.domain.rolechange.domain.UserRole;

public record CreateRoleChange(
        UserRole roleChange
) {
    public RoleChange toRoleChangeEntity(User user) {
        return RoleChange.from(
            user,
            roleChange,
            RequestStatus.PENDING
        );
    }
}
