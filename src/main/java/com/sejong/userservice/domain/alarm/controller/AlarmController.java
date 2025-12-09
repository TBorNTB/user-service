package com.sejong.userservice.domain.alarm.controller;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.application.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/received")
    @Operation(summary = "받은 알람 표시 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<List<AlarmDto>> getAllAlarm(
            @RequestParam("alarmType") AlarmType alarmType
    ) {
        UserContext currentUser = getCurrentUser();
        List<AlarmDto> alarms = alarmService.findAll(currentUser.getUsername(),alarmType);
        return ResponseEntity.status(HttpStatus.OK).body(alarms);
    }

    @PostMapping("/{alarmId}/seen")
    @Operation(summary = "알람 읽음 처리 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> markAsSeen(
            @PathVariable("alarmId") Long alarmId
    ) {
        UserContext currentUser = getCurrentUser();
        alarmService.markAsSeen(currentUser.getUsername(), alarmId);
        return ResponseEntity.ok().build();
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
