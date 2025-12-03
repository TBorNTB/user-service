package com.sejong.userservice.core.common.pagination;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CursorPageResponse<T> {

    private T content;
    private Cursor nextCursor;
    private boolean hasNext;

    private static <T> CursorPageResponse<T> ok(Cursor nextCursor, boolean hasNext, T content) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 커서 기반 페이지네이션을 위한 정적 생성 메서드
     * (전체 데이터에서 size+1로 받은 리스트를 기반으로 생성)
     */
    public static <T> CursorPageResponse<List<T>> from(
            List<T> fullContent,
            int requestedSize,
            Function<T, Cursor> cursorExtractor
    ) {
        boolean hasNext = fullContent.size() > requestedSize;

        List<T> content = hasNext
                ? fullContent.subList(0, requestedSize)
                : fullContent;

        Cursor nextCursor = hasNext && !content.isEmpty()
                ? cursorExtractor.apply(content.get(content.size() - 1)) //마지막 내용물을 받아서 Cursor 생성자 생성
                : null;

        return CursorPageResponse.ok(nextCursor, hasNext, content);
    }
}