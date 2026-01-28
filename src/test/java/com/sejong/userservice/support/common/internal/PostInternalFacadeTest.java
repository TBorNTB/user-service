package com.sejong.userservice.support.common.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sejong.userservice.support.common.constants.PostType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostInternalFacadeTest {

    @Mock
    private ProjectInternalService projectInternalService;

    @Mock
    private NewsInternalService newsInternalService;

    @Mock
    private CSKnowledgeInternalService csKnowledgeInternalService;

    @Mock
    private QnaInternalService qnaInternalService;

    @Test
    void checkPostExistenceAndOwner_routesQnaQuestionToQnaInternalService() {
        PostInternalFacade facade = new PostInternalFacade(
                projectInternalService,
                newsInternalService,
                csKnowledgeInternalService,
                qnaInternalService
        );

        when(qnaInternalService.validateQuestionExists(10L)).thenReturn("owner");

        String owner = facade.checkPostExistenceAndOwner(10L, PostType.QNA_QUESTION);

        assertThat(owner).isEqualTo("owner");
        verify(qnaInternalService).validateQuestionExists(10L);
    }

    @Test
    void checkPostExistenceAndOwner_routesQnaAnswerToQnaInternalService() {
        PostInternalFacade facade = new PostInternalFacade(
                projectInternalService,
                newsInternalService,
                csKnowledgeInternalService,
                qnaInternalService
        );

        when(qnaInternalService.validateAnswerExists(20L)).thenReturn("owner2");

        String owner = facade.checkPostExistenceAndOwner(20L, PostType.QNA_ANSWER);

        assertThat(owner).isEqualTo("owner2");
        verify(qnaInternalService).validateAnswerExists(20L);
    }
}
