package com.hashed.ecombend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration.
 * Swagger UI available at: http://localhost:8080/swagger-ui.html
 * The @SecurityScheme annotation adds the "Authorize" button to Swagger UI
 * so you can paste a JWT and test protected endpoints directly from the browser.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Ecombend API",
        version = "1.0",
        description = "E-commerce REST API built with Spring Boot 4"
))
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
