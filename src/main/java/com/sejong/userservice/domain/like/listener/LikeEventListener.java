package com.sejong.userservice.domain.like.listener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.sejong.userservice.support.common.kafka.DomainAlarmEvent;
import com.sejong.userservice.support.common.kafka.EventPublisher;
import com.sejong.userservice.support.common.kafka.PostLikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private final EventPublisher postlikeEventPublisher;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleLikeCreated(PostLikeEvent event) {
        // DB 커밋 후에만 Kafka 발행
        postlikeEventPublisher.publishLike(event);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleLikedAlarm(DomainAlarmEvent event) {
        // DB 커밋 후에만 Kafka 발행
        postlikeEventPublisher.publishLikedAlarm(event);

    }
}
