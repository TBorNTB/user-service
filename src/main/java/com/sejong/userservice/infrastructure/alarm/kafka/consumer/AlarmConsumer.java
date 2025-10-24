package com.sejong.userservice.infrastructure.alarm.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = TopicNames.ALARM,
            groupId = GroupNames.ALARM
    )
    public void consume(String message)  {
        try {
            // Step 1: JSON → Map
            Map<String, Object> raw = objectMapper.readValue(message, Map.class);
            String type = (String) raw.get("type");

            // Step 2: type에 따라 클래스 선택
            Class<? extends AlarmEvent> targetClass = switch (type) {
                case "LIKE_RECEIVED" -> PostLikeAlarmEvent.class;
//                case "COMMENT_ADDED" -> CommentAddedEvent.class;
                default -> throw new IllegalArgumentException("Unknown event type: " + type);
            };

            // Step 3: 다시 해당 타입으로 변환
            AlarmEvent event = objectMapper.convertValue(raw, targetClass);

            // Step 4: 처리 로직
            handleEvent(event);

        } catch (Exception e) {
            log.error("Failed to consume message: {}", message, e);
        }
    }

    private void handleEvent(AlarmEvent event) {
        if (event instanceof PostLikeAlarmEvent like) {
            log.info("💬 Like event from {} to {}", like.getActorUsername(), like.getOwnerUsername());
        }
    }

}
