package com.sejong.userservice.core.alarm;

import com.sejong.userservice.application.alarm.dto.AlarmDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Alarm {
    private AlarmType alarmType;
    private DomainType domainType;
    private Long domainId;
    private String actorUsername;
    private String ownerUsername;
    private String message;
    private boolean isSeen;
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
                .isSeen(false)
                .createdAt(alarmDto.getCreatedAt())
                .build();
    }

    private static String buildMessage(AlarmType alarmType, DomainType domainType, String actorNickname) {
        String domainKorean = switch (domainType) {
            case PROJECT -> "프로젝트";
            case NEWS -> "뉴스";
            case ARCHIVE -> "아카이브";
            case GLOBAL -> "";
        };

        return switch (alarmType) {
            case COMMENT_ADDED -> String.format("%s님이 %s에 댓글을 남겼습니다.", actorNickname, domainKorean);

            case COMMENT_REPLY_ADDED -> String.format("%s님이 %s에 댓글 응답을 남겼습니다.", actorNickname, domainKorean);

            case POST_LIKED -> String.format("%s님이 %s를 좋아합니다.", actorNickname, domainKorean);

            case SIGNUP -> String.format("🎉 %s님이 새로 가입했습니다!", actorNickname);

            default -> "새로운 알림이 있습니다.";
        };
    }

}
