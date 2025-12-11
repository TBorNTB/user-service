package com.sejong.userservice.support.common.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cursor {
    private Long projectId;

    public static Cursor of(Long projectId) {
        return Cursor.builder()
                .projectId(projectId)
                .build();
    }
}
