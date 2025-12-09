package com.sejong.userservice.domain.rolechange.dto.response;

import com.sejong.userservice.domain.rolechange.domain.RoleChangeEntity;

public record RoleChangeResponse(
    RoleChangeEntity roleChange
) {
}
