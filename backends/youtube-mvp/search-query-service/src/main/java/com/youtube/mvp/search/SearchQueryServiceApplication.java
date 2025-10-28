package com.youtube.mvp.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Search Query Service.
 */
@SpringBootApplication
@EnableAsync
public class SearchQueryServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SearchQueryServiceApplication.class, args);
    }
}
