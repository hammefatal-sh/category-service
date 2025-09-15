package com.example.categoryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> health() {
        return Map.of(
            "service", "Category Service",
            "status", "UP",
            "timestamp", LocalDateTime.now()
        );
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}