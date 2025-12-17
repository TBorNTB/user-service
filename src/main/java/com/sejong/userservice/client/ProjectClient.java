package com.sejong.userservice.client;

import com.sejong.metaservice.support.common.internal.response.PostLikeCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service", path = "/internal")
public interface ProjectClient {
    @GetMapping("/project/check/{postId}")
    ResponseEntity<PostLikeCheckResponse> checkProject(@PathVariable("postId") Long postId);

    @GetMapping("/project/count")
    ResponseEntity<Long> getProjectCount();

    @GetMapping("/category/count")
    ResponseEntity<Long> getCategoryCount();
}