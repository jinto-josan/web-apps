package com.youtube.edgecdncontrol.infrastructure.config;

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
    public OpenAPI edgeCdnControlServiceApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Edge/CDN Control Service API")
                        .version("v1")
                        .description("API for managing Azure Front Door/CDN rules, WAF configs, and cache purging")
                        .contact(new Contact()
                                .name("Platform Team")
                                .email("platform@youtube.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://youtube.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.youtube.com").description("Production")
                ));
    }
}

