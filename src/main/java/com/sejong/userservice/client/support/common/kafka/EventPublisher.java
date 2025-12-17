package com.sejong.userservice.client.support.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.metaservice.domain.comment.domain.Comment;
import com.sejong.metaservice.domain.like.domain.Like;
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
    public void publishLike(Like like, Long postCount){
        log.info("발행 시작 좋아요 postLike :{}, postCount : {}", like, postCount);
        PostLikeEvent event = PostLikeEvent.of(like.getPostType(), like.getPostId(), postCount);
        String key = "post:" + like.getPostId();
        kafkaTemplate.send(topic,key, toJsonString(event));
    }

    public void publishLikedAlarm(Like like, String ownerUsername){
        log.info("알람 이벤트 발행 시작 postLike :{}, ownerUsername : {}", like, ownerUsername);
        DomainAlarmEvent event = DomainAlarmEvent.from(like, AlarmType.POST_LIKED ,ownerUsername);
        String key = "alarm-like:" + like.getPostId();
        kafkaTemplate.send(alarmTopic,key, toJsonString(event));
    }

    public void publishCommentAlarm(Comment savedComment, String ownerUsername) {
        log.info("알람 이벤트 발행 시작 comment :{}", savedComment);
        DomainAlarmEvent event = DomainAlarmEvent.from(savedComment, AlarmType.COMMENT_ADDED, ownerUsername);
        String key = "alarm-comment:" + savedComment.getId();
        kafkaTemplate.send(alarmTopic,key, toJsonString(event));
    }

    public void publishReplyAlarm(Comment parentComment, Comment reply) {
        log.info("알람 이벤트 발행 시작 parentComment: {}, reply: {}", parentComment.getId(), reply.getId());
        DomainAlarmEvent event = DomainAlarmEvent.fromReply(parentComment, reply, AlarmType.COMMENT_REPLY_ADDED);
        String key = "alarm-reply:" + reply.getId();
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
