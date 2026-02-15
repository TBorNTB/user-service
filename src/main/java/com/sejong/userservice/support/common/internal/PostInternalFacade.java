package com.sejong.userservice.support.common.internal;

import static com.sejong.userservice.support.common.exception.type.ExceptionType.BAD_REQUEST;

import com.sejong.userservice.domain.meta.MetaPostCountDto;
import com.sejong.userservice.support.common.constants.PostType;
import com.sejong.userservice.support.common.exception.type.BaseException;

import java.util.List;
import java.util.Map;

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
    private final QnaInternalService qnaInternalService;

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
            case QNA_QUESTION -> {
                return qnaInternalService.validateQuestionExists(postId);
            }
            case QNA_ANSWER -> {
                return qnaInternalService.validateAnswerExists(postId);
            }
            default -> throw new BaseException(BAD_REQUEST);

        }
    }

    // MetaService에서 이미 async로 호출하므로 여기서는 직렬 호출
    public MetaPostCountDto getPostCount() {
        Long projectCount = projectInternalService.getProjectCount();
        Long articleCount = CSKnowledgeInternalService.getCsCount();
        Long categoryCount = projectInternalService.getCategoryCount();
        Long newsCount = projectInternalService.getCsCount();
        return MetaPostCountDto.of(projectCount, newsCount, articleCount, categoryCount);
    }

    /**
     * 사용자가 작성한 모든 글의 ID 목록을 가져옵니다.
     *
     * @param username 사용자 이름
     * @return PostType별로 그룹화된 글 ID 목록
     */
    public Map<PostType, List<Long>> getUserPostIds(String username) {
        List<Long> projectIds = projectInternalService.getUserProjectIds(username);
        List<Long> newsIds = newsInternalService.getUserNewsIds(username);
        List<Long> articleIds = CSKnowledgeInternalService.getUserCsKnowledgeIds(username);

        return Map.of(
                PostType.PROJECT, projectIds,
                PostType.NEWS, newsIds,
                PostType.ARTICLE, articleIds
        );
    }
}
