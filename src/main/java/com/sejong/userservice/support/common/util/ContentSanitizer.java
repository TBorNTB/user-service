package com.sejong.userservice.support.common.util;

import org.springframework.lang.Nullable;

/**
 * 사용자 입력 텍스트의 XSS(Cross-Site Scripting) 방지를 위한 정제 계약.
 * 댓글/대댓글, 채팅 메시지 등 HTML 컨텍스트에 노출되는 텍스트에 사용한다.
 */
public interface ContentSanitizer {

    /**
     * HTML에 안전하게 삽입할 수 있도록 특수문자를 이스케이프한다.
     * 저장 시점 또는 응답 직전에 호출하여, 프론트에서 그대로 노출해도 스크립트가 실행되지 않는다.
     *
     * @param input 사용자 입력 (null/blank 허용)
     * @return 이스케이프된 문자열. null 입력 시 null, blank 시 trim 후 반환
     */
    @Nullable
    String sanitize(@Nullable String input);

    /**
     * null이 아닌 값만 sanitize하고, null이면 그대로 null 반환.
     * DTO/엔티티 필드별 처리 시 편의용.
     */
    @Nullable
    default String sanitizeOrNull(@Nullable String input) {
        return input == null ? null : sanitize(input);
    }
}
