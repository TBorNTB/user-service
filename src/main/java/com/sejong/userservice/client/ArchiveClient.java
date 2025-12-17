package com.sejong.userservice.client;

import com.sejong.userservice.support.common.internal.response.PostLikeCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "archive-service", path = "/internal/archive")
public interface ArchiveClient {

    @GetMapping("/check/news/{newsId}")
    ResponseEntity<PostLikeCheckResponse> checkNews(@PathVariable("newsId") Long newsId);

    @GetMapping("/check/cs/{csKnowledgeId}")
    ResponseEntity<PostLikeCheckResponse> checkCSKnowledge(@PathVariable("csKnowledgeId") Long csKnowledgeId);

    @GetMapping("/news/count")
    ResponseEntity<Long> getNewsCount();

    @GetMapping("/cs/count")
    ResponseEntity<Long> getCsCount();
}
