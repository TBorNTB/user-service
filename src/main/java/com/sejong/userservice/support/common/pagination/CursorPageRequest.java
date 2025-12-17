package com.sejong.userservice.support.common.pagination;


import lombok.Getter;

@Getter
public class CursorPageRequest {
    Cursor cursor;
    int size;
    String sortBy;
    SortDirection direction;

    private CursorPageRequest(Cursor cursor, int size, String sortBy, SortDirection direction) {
        this.cursor = cursor;
        this.size = size;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public static CursorPageRequest of(Cursor cursor, int size, String sortBy, SortDirection direction) {
        return new CursorPageRequest(cursor, size, sortBy, direction);
    }
}
