package com.sejong.userservice.support.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.userservice.support.common.kafka.event.DomainAlarmEvent;
import com.sejong.userservice.support.common.kafka.event.PostLikeEvent;
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
    private final String topic = "postlike";
    private final String alarmTopic = "alarm";

    // listener: elastic-service
    public void publishLike(PostLikeEvent event){
        log.info("발행 시작 좋아요 like event :{}", event);
        String key = "post:" + event.getPostId();
        kafkaTemplate.send(topic,key, toJsonString(event));
    }

    public void publishLikedAlarm(DomainAlarmEvent event){
        log.info("알람 이벤트 발행 시작 event :{}", event);
        String key = "alarm-like:" + event.getDomainId();
        kafkaTemplate.send(alarmTopic, key, toJsonString(event));
    }

    public void publishCommentAlarm(DomainAlarmEvent event) {
        String key = "alarm-comment:" + event.getDomainId();
        kafkaTemplate.send(alarmTopic, key, toJsonString(event));
    }

    public void publishReplyAlarm(DomainAlarmEvent event) {
        String key = "alarm-reply:" + event.getDomainId();
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
