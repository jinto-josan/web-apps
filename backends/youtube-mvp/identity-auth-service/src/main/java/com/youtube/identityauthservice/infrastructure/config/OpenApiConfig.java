package com.youtube.identityauthservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Auth Service API")
                        .description("A comprehensive authentication and authorization service for the YouTube MVP platform, " +
                                "supporting multiple authentication flows including local login, Azure AD B2C integration, " +
                                "device flow, and Multi-Factor Authentication (MFA).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("YouTube MVP Team")
                                .email("support@youtubemvp.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://identity-auth-service.local")
                                .description("Production Server")
                ));
    }
}
