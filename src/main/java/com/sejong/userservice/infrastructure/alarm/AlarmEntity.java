package com.sejong.userservice.infrastructure.alarm;

import com.sejong.userservice.core.alarm.Alarm;
import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.core.alarm.DomainType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alarms")
public class AlarmEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ownerUsername;
    private String actorUsername;
    private Long domainId;
    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;
    @Enumerated(EnumType.STRING)
    private DomainType domainType;
    private String message;
    @Column(name = "is_seen")
    private boolean isSeen;
    private LocalDateTime createdAt;

    public static AlarmEntity fromAlarm(Alarm alarm) {
        return AlarmEntity.builder()
                .ownerUsername(alarm.getOwnerUsername())
                .actorUsername(alarm.getActorUsername())
                .alarmType(alarm.getAlarmType())
                .domainId(alarm.getDomainId())
                .domainType(alarm.getDomainType())
                .message(alarm.getMessage())
                .isSeen(alarm.isSeen())
                .createdAt(alarm.getCreatedAt())
                .build();
    }

    public Alarm toDomain() {
        return Alarm.builder()
                .ownerUsername(ownerUsername)
                .actorUsername(actorUsername)
                .domainId(domainId)
                .alarmType(alarmType)
                .domainType(domainType)
                .message(message)
                .domainId(domainId)
                .createdAt(createdAt)
                .build();
    }
}