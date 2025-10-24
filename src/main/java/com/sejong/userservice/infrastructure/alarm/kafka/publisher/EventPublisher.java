package com.sejong.metaservice.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.metaservice.core.comment.domain.Comment;
import com.sejong.metaservice.core.postlike.domain.PostLike;
import com.sejong.metaservice.core.reply.domain.Reply;
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

    public void publishLike(PostLike postLike, Long postCount){
        log.info("발행 시작 좋아요 postLike :{}, postCount : {}", postLike, postCount);
        PostLikeEvent event = PostLikeEvent.of(postLike.getPostType(),postLike.getPostId(), postCount);
        String key = "post:" + postLike.getPostId();
        kafkaTemplate.send(topic,key, toJsonString(event));
    }

    public void publishLikedAlarm(PostLike postLike, String ownerUsername){
        log.info("알람 이벤트 발행 시작 postLike :{}, ownerUsername : {}", postLike, ownerUsername);
        DomainAlarmEvent event = DomainAlarmEvent.from(postLike,AlarmType.POST_LIKED ,ownerUsername);
        String key = "alarm-like:" + postLike.getPostId();
        kafkaTemplate.send(alarmTopic,key, toJsonString(event));
    }

    public void publishCommentAlarm(Comment savedComment, String ownerUsername) {
        log.info("알람 이벤트 발행 시작 comment :{}", savedComment);
        DomainAlarmEvent event = DomainAlarmEvent.from(savedComment, AlarmType.COMMENT_ADDED, ownerUsername);
        String key = "alarm-comment:" + savedComment.getId();
        kafkaTemplate.send(alarmTopic,key, toJsonString(event));
    }

    public void publishReplyAlarm(Comment parentComment, Reply responseReply) {
        log.info("알람 이벤트 발행 시작 comment :{}, responseReply : {}", parentComment);
        DomainAlarmEvent event = DomainAlarmEvent.from(parentComment, responseReply, AlarmType.COMMENT_REPLY_ADDED);
        String key = "alarm-reply:" + responseReply.getId();
        kafkaTemplate.send(alarmTopic,key, toJsonString(event));
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
