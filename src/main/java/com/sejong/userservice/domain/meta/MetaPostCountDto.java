package com.sejong.userservice.domain.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaPostCountDto {

    private Long projectCount;
    private Long articleCount;
    private Long categoryCount;

    public static MetaPostCountDto of(Long projectCount, Long articleCount, Long categoryCount) {
        return MetaPostCountDto
            .builder()
            .projectCount(projectCount)
            .articleCount(articleCount)
            .categoryCount(categoryCount)
            .build();
    }
}
