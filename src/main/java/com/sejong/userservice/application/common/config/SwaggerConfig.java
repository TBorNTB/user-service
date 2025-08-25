package com.sejong.userservice.application.common.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/user-service"),
                @Server(url = "/")
        },
        info = @Info(
                title = "UserService API",
                version = "v1",
                description = "User API 문서입니다."
        )
)
@Configuration
public class SwaggerConfig {
        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                        );
        }

}