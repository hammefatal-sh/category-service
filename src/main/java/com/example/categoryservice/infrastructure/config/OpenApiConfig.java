package com.example.categoryservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI categoryServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Category Service API")
                .description("온라인 쇼핑몰 카테고리 관리 서비스")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@example.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development Server")
            ));
    }
}