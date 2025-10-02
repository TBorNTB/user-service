package com.sejong.userservice.application.user;

import com.sejong.userservice.core.user.RoleChange;
import com.sejong.userservice.core.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/role")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/all")
    public ResponseEntity<List<RoleChange>> getAllUserRoles() {
        List<RoleChange> roleChanges = userRoleService.findAll();
        return ResponseEntity.ok(roleChanges);
    }
}
