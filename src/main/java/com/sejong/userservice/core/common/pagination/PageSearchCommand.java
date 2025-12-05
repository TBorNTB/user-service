package com.sejong.userservice.core.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageSearchCommand {
    private int size;
    private LocalDateTime cursor;
    private String direction;
    private String sort;

    public static PageSearchCommand of(int size, LocalDateTime cursor, String direction, String sort) {
        return PageSearchCommand.builder()
                .size(size)
                .cursor(cursor)
                .sort(sort)
                .direction(direction)
                .build();
    }
}
