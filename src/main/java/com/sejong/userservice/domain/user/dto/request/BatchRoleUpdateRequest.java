package com.sejong.userservice.domain.user.dto.request;

import com.sejong.userservice.domain.role.domain.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchRoleUpdateRequest {
    @NotEmpty(message = "username 목록은 필수입니다.")
    private List<String> usernames;
    
    @NotNull(message = "변경할 역할은 필수입니다.")
    private UserRole role;
}

