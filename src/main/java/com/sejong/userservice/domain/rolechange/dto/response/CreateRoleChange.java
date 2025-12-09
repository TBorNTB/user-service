package com.sejong.userservice.domain.rolechange.dto.response;

import com.sejong.userservice.core.user.User;
import com.sejong.userservice.domain.rolechange.domain.RequestStatus;
import com.sejong.userservice.domain.rolechange.domain.RoleChange;
import com.sejong.userservice.domain.rolechange.domain.UserRole;
import java.time.LocalDateTime;

public record CreateRoleChange(
        UserRole roleChange
) {
    public RoleChange toRoleChangeEntity(User user) {
        return RoleChange.builder()
            .id(null)
            .realName(user.getRealName())
            .previousRole(user.getRole())
            .requestedRole(roleChange)
            .email(user.getEmail())
            .requestedAt(LocalDateTime.now())
            .processedAt(LocalDateTime.now())
            .requestStatus(RequestStatus.PENDING)
            .processedBy(null)
            .build();
    }
}
