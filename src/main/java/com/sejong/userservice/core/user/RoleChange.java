package com.sejong.userservice.core.user;

import com.sejong.userservice.infrastructure.rolechange.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleChange {

    private Long id;
    private String realName;

    private UserRole role;
    private String requestedRole;
    private String email;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    private RequestStatus requestStatus;
    private String processedBy;

    public static RoleChange from(User user, String requestedRole, RequestStatus requestStatus) {
        return RoleChange.builder()
                .id(null)
                .realName(user.getRealName())
                .role(user.getRole())
                .requestedRole(requestedRole)
                .email(user.getEmail())
                .requestedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .requestStatus(requestStatus)
                .processedBy(null)
                .build();
    }

}
