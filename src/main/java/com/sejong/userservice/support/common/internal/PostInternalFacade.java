package com.sejong.userservice.support.common.internal;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.BAD_REQUEST;

import com.sejong.userservice.domain.meta.MetaPostCountDto;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostInternalFacade {

    private final ProjectInternalService projectInternalService;
    private final NewsInternalService newsInternalService;
    private final CSKnowledgeInternalService CSKnowledgeInternalService;

    public String checkPostExistenceAndOwner(Long postId, PostType postType) {
        switch (postType) {
            case NEWS -> {
                return newsInternalService.validateExists(postId);
            }
            case PROJECT -> {
                return projectInternalService.validateExists(postId);
            }
            case ARTICLE -> {
                return CSKnowledgeInternalService.validateExists(postId);
            }
            default -> throw new BaseException(BAD_REQUEST);

        }
    }

    // MetaService에서 이미 async로 호출하므로 여기서는 직렬 호출
    public MetaPostCountDto getPostCount() {
        Long projectCount = projectInternalService.getProjectCount();
        Long articleCount = CSKnowledgeInternalService.getCsCount();
        Long newsCount = projectInternalService.getCsCount();

        return MetaPostCountDto.of(projectCount, articleCount, newsCount);
    }
}
