package com.youtube.contentidservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContentIdServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentIdServiceApplication.class, args);
    }
}

