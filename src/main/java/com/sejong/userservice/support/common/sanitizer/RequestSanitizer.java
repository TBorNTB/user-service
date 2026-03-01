package com.sejong.userservice.support.common.sanitizer;

import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.comment.dto.command.CommentCommand;
import com.sejong.userservice.domain.comment.dto.request.CommentReq;
import org.springframework.lang.Nullable;

/**
 * XSS 방어 정책의 계약.
 * 요청/입력별로 sanitize 대상을 정의하며, 정책 변경 시 구현체만 수정하면 된다.
 */
public interface RequestSanitizer {

    /**
     * 댓글/대댓글 생성용 명령의 content를 정제한 새 CommentCommand를 반환한다.
     */
    CommentCommand sanitize(CommentCommand command);

    /**
     * 댓글 수정 요청을 원본 객체에 반영하여 정제한다 (content).
     */
    void sanitize(CommentReq request);

    /**
     * 채팅 메시지 DTO의 사용자 입력 필드(content, nickname 등)를 정제한 새 DTO를 반환한다.
     */
    ChatMessageDto sanitize(ChatMessageDto dto);

    /**
     * 단일 문자열(content)만 정제한다. 댓글 수정 등에서 사용.
     */
    @Nullable
    String sanitizeContent(@Nullable String content);
}
