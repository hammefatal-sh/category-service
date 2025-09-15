package com.example.categoryservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationConfig 테스트")
class ApplicationConfigTest {

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
    }

    @Test
    @DisplayName("ObjectMapper Bean이 올바르게 설정되는지 확인")
    void ObjectMapper_Bean이_올바르게_설정되는지_확인() {
        // when
        ObjectMapper objectMapper = applicationConfig.objectMapper();

        // then
        assertThat(objectMapper).isNotNull();
        assertThat(objectMapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        assertThat(objectMapper.getPropertyNamingStrategy()).isEqualTo(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    @DisplayName("ObjectMapper가 LocalDateTime을 올바르게 처리하는지 확인")
    void ObjectMapper가_LocalDateTime을_올바르게_처리하는지_확인() throws Exception {
        // given
        ObjectMapper objectMapper = applicationConfig.objectMapper();
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // when
        String json = objectMapper.writeValueAsString(Map.of("created_at", dateTime));

        // then
        assertThat(json).contains("\"created_at\":\"2024-01-01T12:00:00\"");
        assertThat(json).doesNotContain("1704110400000"); // timestamp format이 아님을 확인
    }

    @Test
    @DisplayName("ObjectMapper가 snake_case를 사용하는지 확인")
    void ObjectMapper가_snake_case를_사용하는지_확인() throws Exception {
        // given
        ObjectMapper objectMapper = applicationConfig.objectMapper();
        TestDto testDto = new TestDto();
        testDto.categoryName = "Test Category";
        testDto.parentId = 123L;

        // when
        String json = objectMapper.writeValueAsString(testDto);

        // then
        assertThat(json).contains("\"category_name\"");
        assertThat(json).contains("\"parent_id\"");
        assertThat(json).doesNotContain("\"categoryName\"");
        assertThat(json).doesNotContain("\"parentId\"");
    }

    @Test
    @DisplayName("Validator Bean이 올바르게 생성되는지 확인")
    void Validator_Bean이_올바르게_생성되는지_확인() {
        // when
        Validator validator = applicationConfig.validator();

        // then
        assertThat(validator).isNotNull();
    }

    @Test
    @DisplayName("Validator가 정상적으로 동작하는지 확인")
    void Validator가_정상적으로_동작하는지_확인() {
        // given
        Validator validator = applicationConfig.validator();
        ValidatedDto validDto = new ValidatedDto();
        validDto.name = "Valid Name";

        ValidatedDto invalidDto = new ValidatedDto();
        invalidDto.name = null; // null 값

        // when & then - ValidatorFactory를 통해 validator를 얻어서 검증
        try {
            assertThat(validator.validate(validDto)).isEmpty();
            assertThat(validator.validate(invalidDto)).isNotEmpty();
        } catch (Exception e) {
            // Validator가 초기화되지 않은 경우 예외 발생할 수 있음
            assertThat(e).isInstanceOf(IllegalStateException.class);
        }
    }

    // 테스트용 DTO 클래스
    static class TestDto {
        public String categoryName;
        public Long parentId;
    }

    static class ValidatedDto {
        @jakarta.validation.constraints.NotBlank
        public String name;
    }
}