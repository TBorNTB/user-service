package com.sejong.userservice.support.common.util;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * {@link ContentSanitizer}의 HTML 이스케이프 기반 구현.
 * &lt;, &gt;, &amp;, &quot;, &#39; 등으로 변환하여 스크립트 실행을 방지한다.
 */
@Component
public class HtmlEscapeContentSanitizer implements ContentSanitizer {

    @Override
    @Nullable
    public String sanitize(@Nullable String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return HtmlUtils.htmlEscape(trimmed);
    }
}
