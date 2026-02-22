package com.sejong.userservice.domain.alarm.controller;

import com.sejong.userservice.domain.alarm.controller.dto.AlarmBulkRequest;
import com.sejong.userservice.domain.alarm.controller.dto.AlarmDto;
import com.sejong.userservice.domain.alarm.controller.dto.AlarmUnreadCountRes;
import com.sejong.userservice.domain.alarm.domain.AlarmType;
import com.sejong.userservice.domain.alarm.service.AlarmService;
import com.sejong.userservice.support.common.pagination.OffsetPageReq;
import com.sejong.userservice.support.common.pagination.OffsetPageRes;
import com.sejong.userservice.support.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/received")
    @Operation(summary = "받은 알람 표시 api (미확인만, 타입 필수)", description = "기존 호환용. 페이지네이션 필요 시 /received/page 사용")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<List<AlarmDto>> getAllAlarm(
            @RequestParam("alarmType") AlarmType alarmType
    ) {
        UserContext currentUser = getCurrentUser();
        List<AlarmDto> alarms = alarmService.findAll(currentUser.getUsername(), alarmType);
        return ResponseEntity.status(HttpStatus.OK).body(alarms);
    }

    @GetMapping("/received/page")
    @Operation(summary = "받은 알람 페이지네이션 조회", description = "page, size로 페이징. alarmType 생략 시 전체 타입, seen 생략 시 전체(읽음/안읽음)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public OffsetPageRes<List<AlarmDto>> getAlarmPage(
            @RequestParam(required = false) AlarmType alarmType,
            @RequestParam(required = false) Boolean seen,
            @ModelAttribute @Valid OffsetPageReq offsetPageReq
    ) {
        UserContext currentUser = getCurrentUser();
        var pageable = PageRequest.of(
                offsetPageReq.getPage(),
                offsetPageReq.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<AlarmDto> page = alarmService.findPage(currentUser.getUsername(), alarmType, seen, pageable);
        return OffsetPageRes.ok(page);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "미확인 알람 개수", description = "뱃지 등에 사용")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<AlarmUnreadCountRes> getUnreadCount() {
        UserContext currentUser = getCurrentUser();
        long count = alarmService.getUnreadCount(currentUser.getUsername());
        return ResponseEntity.ok(new AlarmUnreadCountRes(count));
    }

    @PostMapping("/{alarmId}/seen")
    @Operation(summary = "알람 읽음 처리 (단건)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> markAsSeen(
            @PathVariable("alarmId") Long alarmId
    ) {
        UserContext currentUser = getCurrentUser();
        alarmService.markAsSeen(currentUser.getUsername(), alarmId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seen/bulk")
    @Operation(summary = "알람 일괄 읽음 처리", description = "요청한 alarmIds 중 본인 알람만 읽음 처리")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> markAsSeenBulk(@RequestBody @Valid AlarmBulkRequest request) {
        UserContext currentUser = getCurrentUser();
        alarmService.markAsSeenBulk(currentUser.getUsername(), request.getAlarmIds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seen/all")
    @Operation(summary = "전체 알람 읽음 처리", description = "미확인 알람 전체를 읽음 처리")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> markAllAsSeen() {
        UserContext currentUser = getCurrentUser();
        alarmService.markAllAsSeen(currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{alarmId}")
    @Operation(summary = "알람 단건 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> deleteAlarm(@PathVariable("alarmId") Long alarmId) {
        UserContext currentUser = getCurrentUser();
        alarmService.deleteAlarm(currentUser.getUsername(), alarmId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "알람 일괄 삭제", description = "요청한 alarmIds 중 본인 알람만 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> deleteAlarmsBulk(@RequestBody @Valid AlarmBulkRequest request) {
        UserContext currentUser = getCurrentUser();
        alarmService.deleteBulk(currentUser.getUsername(), request.getAlarmIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    @Operation(summary = "읽은 알람 일괄 삭제", description = "이미 읽음 처리된 알람 전체 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENIOR', 'FULL_MEMBER', 'ASSOCIATE_MEMBER', 'GUEST')")
    public ResponseEntity<Void> deleteAllRead() {
        UserContext currentUser = getCurrentUser();
        alarmService.deleteAllRead(currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    private UserContext getCurrentUser() {
        return (UserContext) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
