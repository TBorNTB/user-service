package com.sejong.userservice.client.support.common.pagination.enums;

import static com.sejong.metaservice.support.common.exception.ExceptionType.BAD_SORT_REQUEST;

import com.sejong.metaservice.support.common.exception.BaseException;
import java.util.Arrays;

public enum SortDirection {
    ASC, DESC;

    public static SortDirection from(String name) {
        return Arrays.stream(SortDirection.values())
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new BaseException(BAD_SORT_REQUEST));
    }
}
