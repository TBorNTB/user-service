package com.sejong.userservice.support.common.kafka;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.sejong.userservice.support.common.kafka.event.DomainAlarmEvent;
import com.sejong.userservice.support.common.kafka.event.PostLikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final EventPublisher eventPublisher;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleLikeCreated(PostLikeEvent event) {
        // DB 커밋 후에만 Kafka 발행
        eventPublisher.publishLike(event);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleLikedAlarm(DomainAlarmEvent event) {
        switch (event.getAlarmType()) {
            case POST_LIKED -> eventPublisher.publishLikedAlarm(event);
            case COMMENT_ADDED -> eventPublisher.publishCommentAlarm(event);
            case COMMENT_REPLY_ADDED -> eventPublisher.publishReplyAlarm(event);
        }
    }
}
