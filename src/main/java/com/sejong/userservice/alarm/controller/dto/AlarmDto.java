package com.sejong.userservice.alarm.controller.dto;

import com.sejong.userservice.alarm.domain.AlarmEntity;
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

    public static AlarmDto from(AlarmEntity alarmEntity) {
        return AlarmDto.builder()
                .alarmType(alarmEntity.getAlarmType())
                .domainType(alarmEntity.getDomainType())
                .domainId(alarmEntity.getDomainId())
                .actorUsername(alarmEntity.getActorUsername())
                .ownerUsername(alarmEntity.getOwnerUsername())
                .message(alarmEntity.getMessage())
                .seen(alarmEntity.isSeen())
                .createdAt(alarmEntity.getCreatedAt())
                .build();
    }
}
