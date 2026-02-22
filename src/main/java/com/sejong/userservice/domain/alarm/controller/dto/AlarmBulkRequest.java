package com.sejong.userservice.domain.alarm.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmBulkRequest {

    @NotEmpty(message = "알람 ID 목록은 비어 있을 수 없습니다.")
    private List<Long> alarmIds;
}
