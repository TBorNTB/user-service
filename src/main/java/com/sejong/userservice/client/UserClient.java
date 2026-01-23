package com.sejong.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", path = "/internal")
public interface UserClient {

    @GetMapping("/users/meta/count")
    ResponseEntity<Long> getUserCount();
}
