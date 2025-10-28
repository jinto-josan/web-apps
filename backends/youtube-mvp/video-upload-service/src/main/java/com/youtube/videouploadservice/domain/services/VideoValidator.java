package com.youtube.videouploadservice.domain.services;

import java.io.InputStream;

/**
 * Service for validating video uploads.
 * Ensures uploaded files meet quality and security requirements.
 */
public interface VideoValidator {
    
    /**
     * Validate video file.
     * Checks format, duration, size, etc.
     * 
     * @param inputStream Video file input stream
     * @param fileName Original filename
     * @param fileSize File size in bytes
     * @return Validation result
     */
    ValidationResult validate(InputStream inputStream, String fileName, long fileSize);
    
    /**
     * Result of video validation.
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String detectedMimeType;
        private final long detectedDurationSeconds;
        private final int detectedWidth;
        private final int detectedHeight;
        
        private ValidationResult(boolean valid, String errorMessage, String detectedMimeType,
                                long detectedDurationSeconds, int detectedWidth, int detectedHeight) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.detectedMimeType = detectedMimeType;
            this.detectedDurationSeconds = detectedDurationSeconds;
            this.detectedWidth = detectedWidth;
            this.detectedHeight = detectedHeight;
        }
        
        public static ValidationResult success(String mimeType, long durationSeconds, int width, int height) {
            return new ValidationResult(true, null, mimeType, durationSeconds, width, height);
        }
        
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage, null, 0, 0, 0);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getDetectedMimeType() { return detectedMimeType; }
        public long getDetectedDurationSeconds() { return detectedDurationSeconds; }
        public int getDetectedWidth() { return detectedWidth; }
        public int getDetectedHeight() { return detectedHeight; }
    }
    
    /**
     * Supported video formats.
     */
    enum SupportedFormat {
        MP4("video/mp4"),
        WEBM("video/webm"),
        MOV("video/quicktime"),
        AVI("video/x-msvideo");
        
        private final String mimeType;
        
        SupportedFormat(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public String getMimeType() {
            return mimeType;
        }
    }
    
    /**
     * Upload constraints.
     */
    class Constraints {
        public static final long MAX_FILE_SIZE = 256L * 1024 * 1024 * 1024; // 256 GB
        public static final long MIN_FILE_SIZE = 1024; // 1 KB
        public static final long MAX_DURATION_SECONDS = 3600; // 1 hour
        public static final long MIN_DURATION_SECONDS = 1;
        
        public static boolean isSizeValid(long fileSize) {
            return fileSize >= MIN_FILE_SIZE && fileSize <= MAX_FILE_SIZE;
        }
    }
}

