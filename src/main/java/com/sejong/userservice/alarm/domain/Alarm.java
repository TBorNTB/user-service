package com.sejong.userservice.alarm.domain;

import com.sejong.userservice.alarm.controller.dto.AlarmDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alarms")
public class Alarm {

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

    @Column(name = "seen")
    private boolean seen;

    private LocalDateTime createdAt;

    public static Alarm from(AlarmDto alarmDto, String actorNickname) {
        String message = buildMessage(alarmDto.getAlarmType(), alarmDto.getDomainType(), actorNickname);

        return Alarm.builder()
                .alarmType(alarmDto.getAlarmType())
                .domainType(alarmDto.getDomainType())
                .domainId(alarmDto.getDomainId())
                .actorUsername(alarmDto.getActorUsername())
                .ownerUsername(alarmDto.getOwnerUsername())
                .message(message)
                .seen(false)
                .createdAt(alarmDto.getCreatedAt())
                .build();
    }

    private static String buildMessage(AlarmType alarmType, DomainType domainType, String actorNickname) {
        return switch (alarmType) {
            case COMMENT_ADDED -> String.format("%s님이 %s에 댓글을 남겼습니다.", actorNickname, domainType.getName());

            case COMMENT_REPLY_ADDED -> String.format("%s님이 %s에 댓글 응답을 남겼습니다.", actorNickname, domainType.getName());

            case POST_LIKED -> String.format("%s님이 %s를 좋아합니다.", actorNickname, domainType.getName());

            case SIGNUP -> String.format("🎉 %s님이 새로 가입했습니다!", actorNickname);

            default -> "새로운 알림이 있습니다.";
        };
    }
}