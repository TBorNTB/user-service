package com.sejong.userservice.support.common.sanitizer;

import com.sejong.userservice.domain.chat.dto.ChatMessageDto;
import com.sejong.userservice.domain.comment.dto.command.CommentCommand;
import com.sejong.userservice.domain.comment.dto.request.CommentReq;
import com.sejong.userservice.support.common.util.ContentSanitizer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * XSS 방어 정책의 기본 구현.
 * Sanitize 대상/방식 변경 시 이 클래스만 수정하면 된다.
 */
@Component
public class DefaultRequestSanitizer implements RequestSanitizer {

    private final ContentSanitizer contentSanitizer;

    public DefaultRequestSanitizer(ContentSanitizer contentSanitizer) {
        this.contentSanitizer = contentSanitizer;
    }

    @Override
    public CommentCommand sanitize(CommentCommand command) {
        if (command == null) return null;
        return CommentCommand.builder()
                .username(command.getUsername())
                .postId(command.getPostId())
                .postType(command.getPostType())
                .content(contentSanitizer.sanitize(command.getContent()))
                .parentId(command.getParentId())
                .build();
    }

    @Override
    public void sanitize(CommentReq request) {
        if (request == null) return;
        if (request.getContent() != null) {
            request.setContent(contentSanitizer.sanitize(request.getContent()));
        }
    }

    @Override
    public ChatMessageDto sanitize(ChatMessageDto dto) {
        if (dto == null) return null;
        return ChatMessageDto.builder()
                .type(dto.getType())
                .roomId(dto.getRoomId())
                .username(dto.getUsername())
                .nickname(contentSanitizer.sanitizeOrNull(dto.getNickname()))
                .content(contentSanitizer.sanitizeOrNull(dto.getContent()))
                .imageUrl(dto.getImageUrl())
                .createdAt(dto.getCreatedAt())
                .serverId(dto.getServerId())
                .token(dto.getToken())
                .build();
    }

    @Override
    @Nullable
    public String sanitizeContent(@Nullable String content) {
        return contentSanitizer.sanitize(content);
    }
}
