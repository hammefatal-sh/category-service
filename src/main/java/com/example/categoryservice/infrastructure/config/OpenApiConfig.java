package com.example.categoryservice.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:category-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI categoryServiceOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .tags(tagList())
                .components(apiComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title("Category Service API - 함선홍")
                .description("상품 카테고리 관리를 위한 RESTful API 서비스입니다.\n\n" +
                        "주요 기능:\n" +
                        "- 카테고리 CRUD 작업\n" +
                        "- 계층적 카테고리 트리 구조 관리\n" +
                        "- 캐시를 활용한 고성능 조회\n" +
                        "- 다양한 형태의 카테고리 조회 (단일, 트리, 전체)")
                .version("v1.0.0");
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development Server")
        );
    }

    private List<Tag> tagList() {
        return List.of(
                new Tag()
                        .name("Categories")
                        .description("카테고리 관리 API"),
                new Tag()
                        .name("Health")
                        .description("서비스 상태 확인 API")
        );
    }

    private Components apiComponents() {
        return new Components()
                .schemas(commonSchemas())
                .responses(commonResponses());
    }

    private Map<String, Schema> commonSchemas() {
        Schema<?> errorResponse = new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("string").description("에러 코드"))
                .addProperty("message", new Schema<>().type("string").description("에러 메시지"))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("에러 발생 시간"));

        Map<String, Schema> schemas = new HashMap<>();
        schemas.put("ErrorResponse", errorResponse);
        return schemas;
    }

    private ApiResponses commonResponses() {
        return new ApiResponses()
                .addApiResponse("400", new ApiResponse()
                        .description("잘못된 요청")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                .addApiResponse("404", new ApiResponse()
                        .description("리소스를 찾을 수 없음")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                .addApiResponse("500", new ApiResponse()
                        .description("서버 내부 오류")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));
    }

}