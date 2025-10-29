package com.youtube.moderationservice.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI moderationOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Content Moderation Service API").version("v1").description("Moderation and Policy APIs"))
                .externalDocs(new ExternalDocumentation().description("Docs").url("https://example.com"));
    }
}


