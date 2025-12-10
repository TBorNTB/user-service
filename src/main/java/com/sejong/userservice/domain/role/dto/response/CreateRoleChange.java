package com.sejong.userservice.domain.role.dto.response;

import com.sejong.userservice.domain.role.domain.RequestStatus;
import com.sejong.userservice.domain.role.domain.RoleChange;
import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.user.domain.UserEntity;
import java.time.LocalDateTime;

public record CreateRoleChange(
        UserRole roleChange
) {
    public RoleChange toRoleChangeEntity(UserEntity user) {
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
