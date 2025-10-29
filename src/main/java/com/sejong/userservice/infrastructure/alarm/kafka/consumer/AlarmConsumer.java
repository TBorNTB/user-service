package com.sejong.userservice.infrastructure.alarm.kafka.consumer;

import com.sejong.userservice.application.alarm.AlarmService;
import com.sejong.userservice.application.alarm.dto.AlarmDto;
import com.sejong.userservice.application.common.constants.GroupNames;
import com.sejong.userservice.application.common.constants.TopicNames;
import com.sejong.userservice.infrastructure.alarm.kafka.event.DomainAlarmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmConsumer {

    private final AlarmService alarmService;

    @KafkaListener(
            topics = TopicNames.ALARM,
            groupId = GroupNames.ALARM
    )
    public void consume(String message) {

        DomainAlarmEvent event = DomainAlarmEvent.fromJson(message);
        AlarmDto alarmDto = AlarmDto.from(event);
        alarmService.save(alarmDto);
    }
}
