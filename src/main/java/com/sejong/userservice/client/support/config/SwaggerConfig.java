package com.sejong.userservice.client.support.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/meta-service"),
                @Server(url = "/")
        },
        info = @Info(
                title = "Meta Info API",
                version = "v1",
                description = "댓글, 좋아요, 조회수 등 메타정보를 관리하는 API 문서입니다."
        )
)
@Configuration
public class SwaggerConfig {


}