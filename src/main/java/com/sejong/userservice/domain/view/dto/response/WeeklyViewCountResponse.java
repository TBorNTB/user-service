package com.sejong.userservice.domain.view.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record WeeklyViewCountResponse(
        List<List<Long>> weeklyData  // 4주치 데이터, 각 주는 [월, 화, 수, 목, 금, 토, 일] 순서
) {
}

