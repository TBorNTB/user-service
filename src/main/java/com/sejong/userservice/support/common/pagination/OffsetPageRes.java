package com.sejong.userservice.support.common.pagination;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
@Builder
public class OffsetPageRes<T> {

    private String message;
    private int size;
    private int page;
    private int totalPage;
    private Long totalElement;
    private T data;

    public static <T> OffsetPageRes<List<T>> ok(Page<T> page) {
        return new OffsetPageRes<>("조회 성공", page.getSize(), page.getNumber(), page.getTotalPages(),page.getTotalElements() ,page.getContent());
    }
}
