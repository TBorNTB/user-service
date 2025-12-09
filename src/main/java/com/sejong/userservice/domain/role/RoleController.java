package com.sejong.userservice.domain.role;

import com.sejong.userservice.domain.role.domain.UserRole;
import com.sejong.userservice.domain.role.dto.request.Approval;
import com.sejong.userservice.domain.role.dto.request.RoleChangeRequest;
import com.sejong.userservice.domain.role.dto.response.RoleChangeResponse;
import com.sejong.userservice.support.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users/role")
@RequiredArgsConstructor
public class RoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/all")
    @Operation(summary = "유저 role 갱신 로그 Pending 조회")
    public ResponseEntity<List<RoleChangeResponse>> getAllUserRolesPending() {
        List<RoleChangeResponse> roleChanges = userRoleService.findAll();
        return ResponseEntity.ok(roleChanges);
    }

    @PostMapping("/request")
    @Operation(summary = "유저 role 갱신 요청 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<String> addRoleChange(@RequestBody RoleChangeRequest roleChangeRequest) {
        UserContext currentUser = getCurrentUser();
        String message = userRoleService.addRoleChange(currentUser.getUsername(), roleChangeRequest);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/manage/{roleChangeId}")
    @Operation(summary = "유저 role 승인 및 거절 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> manageRoleChange(
            @PathVariable(name = "roleChangeId") Long roleChangeId,
            @RequestBody Approval request
    ) {
        UserContext currentUser = getCurrentUser();
        String message = userRoleService.manageRoleChange(request.isApproved(), roleChangeId, currentUser.getUsername());
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "전체 유저 Role 조회", description = "모든 userRole을 조회합니다")
    @GetMapping("")
    public ResponseEntity<List<String>> getAllUserRoles() {
        List<String> roles = Arrays.stream(UserRole.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(roles);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
