package com.sejong.userservice.alarm.controller.dto;

import com.sejong.userservice.alarm.domain.Alarm;
import com.sejong.userservice.alarm.domain.AlarmType;
import com.sejong.userservice.alarm.domain.DomainType;
import com.sejong.userservice.alarm.kafka.event.DomainAlarmEvent;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Boolean seen;
    private LocalDateTime createdAt;

    public static AlarmDto from(DomainAlarmEvent event) {
        return AlarmDto.builder()
                .alarmType(event.getAlarmType())
                .domainType(event.getDomainType())
                .domainId(event.getDomainId())
                .actorUsername(event.getActorUsername())
                .ownerUsername(event.getOwnerUsername())
                .message(null)
                .seen(null)
                .createdAt(event.getCreatedAt())
                .build();
    }

    public static AlarmDto from(Alarm alarm) {
        return AlarmDto.builder()
                .alarmType(alarm.getAlarmType())
                .domainType(alarm.getDomainType())
                .domainId(alarm.getDomainId())
                .actorUsername(alarm.getActorUsername())
                .ownerUsername(alarm.getOwnerUsername())
                .message(alarm.getMessage())
                .seen(alarm.isSeen())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
