package com.youtube.edgecdncontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity")
@EnableJpaRepositories(basePackages = "com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa")
public class EdgeCdnControlServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EdgeCdnControlServiceApplication.class, args);
    }
}

