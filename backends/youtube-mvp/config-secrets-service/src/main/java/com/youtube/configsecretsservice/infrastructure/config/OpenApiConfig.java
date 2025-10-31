package com.youtube.configsecretsservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI configSecretsServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Configuration and Secrets Service API")
                        .description("Central configuration and secrets management service with multitenant support")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("YouTube MVP Team")
                                .email("dev@youtube-mvp.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server"),
                        new Server().url("https://api.example.com").description("Production server")
                ));
    }
}

