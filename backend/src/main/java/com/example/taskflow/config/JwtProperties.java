package com.example.taskflow.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Minimum 32 characters (256 bits) required for HMAC-SHA256. */
    @NotBlank
    @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits) for HMAC-SHA256")
    private String secret;

    @Min(1)
    private long expirationMs = 86_400_000L;
}
