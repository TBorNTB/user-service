package com.sejong.userservice.domain.view.controller;

import com.sejong.userservice.domain.view.dto.response.ViewCountResponse;
import com.sejong.userservice.domain.view.dto.response.WeeklyViewCountResponse;
import com.sejong.userservice.domain.view.service.ViewService;
import com.sejong.userservice.support.common.constants.PostType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "조회수 API", description = "게시물 조회수 관련 API")
@RestController
@RequestMapping("/api/view")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    @Operation(summary = "조회수 증가", description = "게시물 조회 시 조회수를 1 증가시킵니다 (중복 조회 방지 적용)")
    @PostMapping("/{postId}")
    public ResponseEntity<ViewCountResponse> increaseViewCount(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "postType") PostType postType,
            HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);
        ViewCountResponse response = viewService.increaseViewCount(postId, postType, clientIp);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "조회수 조회", description = "특정 게시물의 현재 조회수를 조회합니다")
    @GetMapping("/{postId}/count")
    public ResponseEntity<ViewCountResponse> getViewCount(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(name = "postType") PostType postType
    ) {
        ViewCountResponse response = viewService.getViewCount(postId, postType);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기간별 총 조회수 조회", description = "현재일을 기준으로 기간별 총 포스트 조회수를 반환한다.")
    @GetMapping("/count/all/post")
    public ResponseEntity<ViewCountResponse> getAllViewCount(
             @RequestParam(name = "startedDay") Long startedDay,
             @RequestParam(name = "endedDay") Long endedDay
    ){
        ViewCountResponse response = viewService.getAllViewCount(startedDay,endedDay);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 날짜부터 현재까지 총 방문 횟수 조회", 
               description = "특정 날짜(epoch timestamp in milliseconds)부터 현재까지의 총 페이지 방문 횟수를 반환합니다.")
    @GetMapping("/count/total/since")
    public ResponseEntity<ViewCountResponse> getTotalViewCountSince(
            @RequestParam(name = "startDate") Long startDateTimestamp
    ) {
        ViewCountResponse response = viewService.getTotalViewCountSince(startDateTimestamp);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 날짜의 일일 총 조회수 조회", 
               description = "특정 날짜(YYYY-MM-DD 형식)의 총 페이지 방문 횟수를 반환합니다. 예: 2024-01-01")
    @GetMapping("/count/daily")
    public ResponseEntity<ViewCountResponse> getDailyViewCount(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        ViewCountResponse response = viewService.getDailyViewCount(date);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 기간의 일일 총 조회수 합계 조회", 
               description = "시작 날짜부터 종료 날짜까지의 일일 총 조회수 합계를 반환합니다. (YYYY-MM-DD 형식)")
    @GetMapping("/count/daily/between")
    public ResponseEntity<ViewCountResponse> getDailyViewCountBetween(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ViewCountResponse response = viewService.getDailyViewCountBetween(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 날짜부터 현재까지의 일일 총 조회수 합계 조회", 
               description = "특정 날짜(YYYY-MM-DD 형식)부터 현재까지 기록된 일일 총 조회수 합계를 반환합니다.")
    @GetMapping("/count/daily/since")
    public ResponseEntity<ViewCountResponse> getDailyViewCountSince(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        ViewCountResponse response = viewService.getDailyViewCountSince(startDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "N일 전의 일일 총 조회수 조회", 
               description = "현재로부터 N일 전의 일일 총 조회수를 반환합니다. 예: daysAgo=1이면 하루 전")
    @GetMapping("/count/daily/ago")
    public ResponseEntity<ViewCountResponse> getDailyViewCountDaysAgo(
            @RequestParam(name = "daysAgo") Integer daysAgo
    ) {
        LocalDate targetDate = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(daysAgo);
        ViewCountResponse response = viewService.getDailyViewCount(targetDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주간 방문자 수 조회", 
               description = "저번 주부터 시작해서 주간 통계를 반환합니다. " +
                             "각 주는 [월, 화, 수, 목, 금, 토, 일] 순서로 배열됩니다. " +
                             "총 1주치 데이터가 반환됩니다.")
    @GetMapping("/count/weekly")
    public ResponseEntity<WeeklyViewCountResponse> getWeeklyViewCount() {
        WeeklyViewCountResponse response = viewService.getWeeklyViewCount();
        return ResponseEntity.ok(response);
    }



    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}