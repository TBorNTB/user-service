package com.sejong.userservice.infrastructure.alarm.kafka.event;

import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.core.alarm.DomainType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostLikeAlarmEvent implements AlarmEvent{
    private AlarmType alarmType;
    private DomainType domainType;
    private Long domainId;
    private String actorUsername;
    private String ownerUsername;
    private LocalDateTime createdAt;
}