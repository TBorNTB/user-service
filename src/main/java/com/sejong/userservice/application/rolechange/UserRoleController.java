package com.sejong.userservice.application.rolechange;

import com.sejong.userservice.application.common.security.UserContext;
import com.sejong.userservice.application.rolechange.dto.RoleChangeManageRequest;
import com.sejong.userservice.application.rolechange.dto.RoleChangeRequest;
import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/role")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/all")
    @Operation(summary = "유저 role 갱신 로그 Pending 조회")
    public ResponseEntity<List<RoleChange>> getAllUserRolesPending() {
        List<RoleChange> roleChanges = userRoleService.findAll();
        return ResponseEntity.ok(roleChanges);
    }

    @PostMapping("/request")
    @Operation(summary = "유저 role 갱신 요청 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'OUTSIDER')")
    public ResponseEntity<String> addRoleChange(@RequestBody RoleChangeRequest roleChangeRequest) {
        UserContext currentUser = getCurrentUser();
        String message = userRoleService.addRoleChange(currentUser.getUsername(), roleChangeRequest.getRequestRole());
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/manage/{roleChangeId}")
    @Operation(summary = "유저 role 승인 및 거절 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> manageRoleChange(
            @PathVariable(name = "roleChangeId") Long roleChangeId,
            @RequestBody RoleChangeManageRequest request
    ) {
        UserContext currentUser = getCurrentUser();
        String message = userRoleService.manageRoleChange(request.isAccepted(), roleChangeId, currentUser.getUsername());
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
