package com.youtube.contentidservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contentIdServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Content ID Service API")
                        .description("Content ID Service - Fingerprinting, matching, claims/disputes management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("YouTube MVP Team")
                                .email("support@youtube-mvp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server"),
                        new Server().url("https://api.youtube-mvp.com").description("Production server")
                ));
    }
}

