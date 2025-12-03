package com.sejong.userservice.core.common.pagination.enums;


import org.apache.kafka.common.errors.ApiException;

import java.util.Arrays;

public enum SortDirection {
    ASC, DESC;

    public static SortDirection from(String name) {
        return Arrays.stream(SortDirection.values())
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid sort direction: "));
    }
}
