package com.sejong.userservice.infrastructure.alarm.kafka.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.core.alarm.AlarmType;
import com.sejong.userservice.core.user.User;
import com.sejong.userservice.infrastructure.alarm.kafka.event.DomainAlarmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String alarmTopic = "alarm";

    public void publishSignUpAlarm(User user) {
        log.info("알람 이벤트 발행 시작 user :{}", user);
        DomainAlarmEvent event = DomainAlarmEvent.from(user, AlarmType.SIGNUP);
        String key = "alarm-signup:" + user.getId();
        kafkaTemplate.send(alarmTopic, key, toJsonString(event));
    }

    private String toJsonString(Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            return message;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json 직렬화 실패");
        }
    }

}
