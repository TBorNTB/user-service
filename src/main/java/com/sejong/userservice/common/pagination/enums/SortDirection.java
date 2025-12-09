package com.sejong.userservice.common.pagination.enums;


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
