package com.youtube.analyticstelemetryservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${spring.application.name:analytics-telemetry-service}")
    private String applicationName;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Analytics & Telemetry Service API")
                .version("1.0.0")
                .description("Production-grade microservice for collecting and forwarding telemetry events to Azure Event Hubs")
                .contact(new Contact()
                    .name("YouTube MVP Team")
                    .email("support@youtube-mvp.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development"),
                new Server().url("https://api.youtube-mvp.com").description("Production")
            ));
    }
}

