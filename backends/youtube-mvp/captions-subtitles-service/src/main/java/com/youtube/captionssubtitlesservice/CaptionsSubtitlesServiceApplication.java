package com.youtube.captionssubtitlesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Captions and Subtitles Service
 * 
 * Features:
 * - Auto STT (Speech-to-Text) via Azure AI Speech
 * - Human caption editing and versioning
 * - Translations via Azure Translator
 * - SRT/WebVTT storage in Blob Storage
 * - Caption metadata in Cosmos DB
 * - Service Bus event publishing
 * - Idempotent processing with Redis
 * - ETag support for versioning
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CaptionsSubtitlesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaptionsSubtitlesServiceApplication.class, args);
    }
}
