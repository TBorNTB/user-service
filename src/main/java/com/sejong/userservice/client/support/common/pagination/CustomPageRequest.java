package com.sejong.userservice.client.support.common.pagination;

import com.sejong.metaservice.support.common.pagination.enums.SortDirection;
import lombok.Getter;

@Getter
public class CustomPageRequest {
    private final int page;
    private final int size;
    private final String sortBy;
    private final SortDirection direction;

    private CustomPageRequest(int page, int size, String sortBy, SortDirection direction) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public static CustomPageRequest of (int page, int size, String sortBy, SortDirection direction) {
        return new CustomPageRequest(page, size, sortBy, direction);
    }
}