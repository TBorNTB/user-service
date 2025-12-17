package com.sejong.userservice.client.support.common.pagination;

import com.sejong.metaservice.support.common.pagination.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CursorPageRequest {

    private Long cursorId;

    @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100이어야 합니다.")
    private int size = 6;

    private SortDirection direction = SortDirection.DESC;
}
