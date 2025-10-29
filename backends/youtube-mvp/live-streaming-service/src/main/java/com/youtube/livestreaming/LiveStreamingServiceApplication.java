package com.youtube.livestreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LiveStreamingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveStreamingServiceApplication.class, args);
    }
}

