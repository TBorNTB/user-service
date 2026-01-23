package com.sejong.userservice.domain.role.dto.request;

import com.sejong.userservice.domain.role.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleChangeRequest {
    private UserRole requestRole;
}
