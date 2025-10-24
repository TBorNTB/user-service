package com.sejong.userservice.application.alarm.dto;

import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.core.alarm.DomainType;
import com.sejong.userservice.infrastructure.alarm.kafka.event.DomainAlarmEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmDto {
    private AlarmType alarmType;
    private DomainType domainType;
    private Long domainId;
    private String actorUsername;
    private String ownerUsername;
    private String message;
    private LocalDateTime createdAt;

    public static AlarmDto from(DomainAlarmEvent event) {
        return AlarmDto.builder()
                .alarmType(event.getAlarmType())
                .domainType(event.getDomainType())
                .domainId(event.getDomainId())
                .actorUsername(event.getActorUsername())
                .ownerUsername(event.getOwnerUsername())
                .message(null)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
