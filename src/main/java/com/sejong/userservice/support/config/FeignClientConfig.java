package com.sejong.userservice.support.config;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.sejong.userservice.client")
public class FeignClientConfig {
}
