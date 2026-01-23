package com.sejong.userservice.domain.user.dto.request;

import com.sejong.userservice.domain.role.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRoleRequest {
    private UserRole userRole;
}
