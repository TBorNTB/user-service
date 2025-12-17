package com.sejong.userservice.client.support.config;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.sejong.metaservice.client")
public class FeignClientConfig {
}
