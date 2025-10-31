package com.youtube.experimentationservice.infrastructure.config;

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
    public OpenAPI experimentationServiceApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Experimentation Service API")
                        .description("API for feature flags and experiment management")
                        .version("v1")
                        .contact(new Contact()
                                .name("YouTube MVP Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server"),
                        new Server().url("https://api.youtube-mvp.com").description("Production server")));
    }
}

