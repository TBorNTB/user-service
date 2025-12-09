package com.sejong.userservice.domain.rolechange.dto.request;

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
