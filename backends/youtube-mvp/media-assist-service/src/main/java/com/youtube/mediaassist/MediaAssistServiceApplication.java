package com.youtube.mediaassist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MediaAssistServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaAssistServiceApplication.class, args);
    }
}
