package com.fracturecare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Security security,
        Storage storage,
        Ai ai,
        Explanation explanation,
        String frontendOrigin
) {
    public record Security(String jwtSecret, Duration tokenTtl) {}
    public record Storage(Path uploads, Path reports) {}
    public record Ai(String mode, String baseUrl, Duration timeout) {}
    public record Explanation(String mode, String baseUrl, String apiKey, String model, Duration timeout) {}
}
