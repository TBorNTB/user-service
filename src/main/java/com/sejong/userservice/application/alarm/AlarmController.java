package com.sejong.userservice.application.alarm;

import com.sejong.userservice.application.common.security.UserContext;
import com.sejong.userservice.core.alarm.Alarm;
import com.sejong.userservice.core.alarm.AlarmType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {
    private final AlarmService alarmService;

    //토큰에서 정보 꺼내야 된다.
    @GetMapping("/received")
    @Operation(summary = "받은 알람 표시 api")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER')")
    public ResponseEntity<List<Alarm>> getAllAlarm(
            @RequestParam("alarmType") AlarmType alarmType
    ) {
        UserContext currentUser = getCurrentUser();
        List<Alarm> alarms = alarmService.findAll(currentUser.getUsername(),alarmType);
        return ResponseEntity.status(HttpStatus.OK).body(alarms);
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
