package com.sejong.userservice.domain.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaCountAdminResponse {

    private Long projectCount;
    private Long newsCount;
    private Long articleCount;
    private Long categoryCount;

    public static MetaCountAdminResponse of(MetaPostCountDto metaPostCountDto) {
        return MetaCountAdminResponse.builder()
                .projectCount(metaPostCountDto.getProjectCount())
                .newsCount(metaPostCountDto.getNewsCount())
                .articleCount(metaPostCountDto.getArticleCount())
                .categoryCount(metaPostCountDto.getCategoryCount())
                .build();
    }
}
