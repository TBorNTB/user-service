package com.sejong.userservice.support.common.pagination;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
public class OffsetPageReq {

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100이어야 합니다.")
    private int size = 6;

    private SortDirection sortDirection = SortDirection.ASC;

    private String sortBy = "id";

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.valueOf(sortDirection.name());
        return PageRequest.of(page, size, direction, sortBy);
    }
}