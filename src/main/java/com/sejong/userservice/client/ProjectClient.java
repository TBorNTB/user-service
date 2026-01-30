package com.sejong.userservice.client;

import com.sejong.userservice.support.common.internal.response.PostLikeCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "project-service", path = "/internal")
public interface ProjectClient {
    @GetMapping("/project/check/{postId}")
    ResponseEntity<PostLikeCheckResponse> checkProject(@PathVariable("postId") Long postId);

    @GetMapping("/project/count")
    ResponseEntity<Long> getProjectCount();

//    @GetMapping("/category/count")
//    ResponseEntity<Long> getCategoryCount();

    @GetMapping("/project/category/count")
    ResponseEntity<Long> getCategoryCount();

    @GetMapping("/archive/check/news/{newsId}")
    ResponseEntity<PostLikeCheckResponse> checkNews(@PathVariable("newsId") Long newsId);

    @GetMapping("/archive/check/cs/{csKnowledgeId}")
    ResponseEntity<PostLikeCheckResponse> checkCSKnowledge(@PathVariable("csKnowledgeId") Long csKnowledgeId);

    @GetMapping("/qna/check/question/{questionId}")
    ResponseEntity<PostLikeCheckResponse> checkQnaQuestion(@PathVariable("questionId") Long questionId);

    @GetMapping("/qna/check/answer/{answerId}")
    ResponseEntity<PostLikeCheckResponse> checkQnaAnswer(@PathVariable("answerId") Long answerId);

    @GetMapping("/archive/news/count")
    ResponseEntity<Long> getNewsCount();

    @GetMapping("/archive/cs/count")
    ResponseEntity<Long> getCsCount();

    @GetMapping("/project/user/{username}")
    ResponseEntity<List<Long>> getUserProjectIds(@PathVariable("username") String username);

    @GetMapping("/archive/news/user/{username}")
    ResponseEntity<List<Long>> getUserNewsIds(@PathVariable("username") String username);

    @GetMapping("/archive/cs/user/{username}")
    ResponseEntity<List<Long>> getUserCsKnowledgeIds(@PathVariable("username") String username);
}