package com.sejong.userservice.support.common.pagination;


import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CursorPageRes<T> {

    private boolean hasNext;
    private T content;
    private Long nextCursorId;

    public static <T> CursorPageRes<List<T>> from(
            List<T> fullContent,
            int requestedSize,
            Function<T, Long> idExtractor
    ) {
        boolean hasNext = fullContent.size() > requestedSize;

        List<T> content = hasNext
                ? fullContent.subList(0, requestedSize)
                : fullContent;

        Long nextCursorId = hasNext && !content.isEmpty()
                ? idExtractor.apply(content.get(content.size() - 1))
                : null;

        return CursorPageRes.<List<T>>builder()
                .content(content)
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .build();
    }
}