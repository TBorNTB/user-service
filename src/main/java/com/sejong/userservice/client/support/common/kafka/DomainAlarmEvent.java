package com.sejong.userservice.client.support.common.kafka;

import com.sejong.metaservice.domain.comment.domain.Comment;
import com.sejong.metaservice.domain.like.domain.Like;
import com.sejong.metaservice.support.common.enums.PostType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DomainAlarmEvent {

    private AlarmType alarmType;
    private DomainType domainType;
    private Long domainId;
    private String actorUsername; // 좋아요를 누른 사람을 말합니다.
    private String ownerUsername; // 해당 글의 주인을 의미합니다.
    private LocalDateTime createdAt;

    public static DomainAlarmEvent from(Like like, AlarmType alarmType, String ownerUsername) {
        DomainType makeDomainType = getDomainType(like.getPostType());

        return DomainAlarmEvent.builder()
                .domainId(like.getPostId())
                .alarmType(alarmType)
                .domainType(makeDomainType)
                .actorUsername(like.getUsername())
                .ownerUsername(ownerUsername)
                .createdAt(like.getCreatedAt())
                .build();
    }


    public static DomainAlarmEvent from(Comment comment, AlarmType alarmType, String ownerUsername) {
        DomainType makeDomainType = getDomainType(comment.getPostType());

        return DomainAlarmEvent.builder()
                .domainId(comment.getPostId())
                .alarmType(alarmType)
                .domainType(makeDomainType)
                .actorUsername(comment.getUsername())
                .ownerUsername(ownerUsername)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * 대댓글 알림용 - 부모 댓글 작성자에게 알림
     */
    public static DomainAlarmEvent fromReply(Comment parentComment, Comment reply, AlarmType alarmType) {
        DomainType makeDomainType = getDomainType(parentComment.getPostType());

        return DomainAlarmEvent.builder()
                .domainId(parentComment.getPostId())
                .alarmType(alarmType)
                .domainType(makeDomainType)
                .actorUsername(reply.getUsername())
                .ownerUsername(parentComment.getUsername())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    private static DomainType getDomainType(PostType postType) {
        DomainType domainType = switch (postType) {
            case NEWS -> DomainType.NEWS;
            case PROJECT -> DomainType.PROJECT;
            case DOCUMENT -> DomainType.DOCUMENT;
            case ARTICLE -> DomainType.ARTICLE;
            default -> DomainType.GLOBAL;
        };
        return domainType;
    }
}
