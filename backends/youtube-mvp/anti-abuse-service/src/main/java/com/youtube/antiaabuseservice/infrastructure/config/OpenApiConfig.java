package com.youtube.antiaabuseservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI antiAbuseServiceApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Anti-Abuse Service API")
                        .description("API for risk scoring and fraud detection")
                        .version("v1")
                        .contact(new Contact()
                                .name("YouTube MVP Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server"),
                        new Server().url("https://api.youtube-mvp.com").description("Production server")));
    }
}

