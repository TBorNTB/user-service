package com.sejong.userservice.domain.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaCountResponse {

    private Long userCount;
    private Long projectCount;
    private Long articleCount;
    private Long categoryCount;

    public static MetaCountResponse of(Long userCount, MetaPostCountDto metaPostCountDto) {
        return MetaCountResponse.builder()
                .userCount(userCount)
                .projectCount(metaPostCountDto.getProjectCount())
                .articleCount(metaPostCountDto.getArticleCount())
                .categoryCount(metaPostCountDto.getCategoryCount())
                .build();
    }
}
