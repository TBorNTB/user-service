package com.sejong.userservice.application.rolechange.dto;

import com.sejong.userservice.infrastructure.rolechange.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleChangeManageRequest {
    private boolean isAccepted;
}
