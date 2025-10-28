package com.youtube.mediaassist.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Value object for blob path normalization and validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobPath {
    
    private String path;
    private BlobContainer container;
    private String normalizedPath;
    
    public enum BlobContainer {
        SOURCE_VIDEOS("source-videos"),
        RENDITIONS("renditions"),
        MANIFESTS("manifests"),
        THUMBNAILS("thumbnails"),
        SUBTITLES("subtitles"),
        AUDIO("audio");
        
        private final String name;
        
        BlobContainer(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * Normalize and validate the path to prevent directory traversal attacks
     */
    public static BlobPath fromString(String path, BlobContainer container) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        // Normalize the path
        String normalized = Paths.get(path).normalize().toString();
        
        // Prevent directory traversal
        if (normalized.contains("..") || normalized.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: directory traversal not allowed");
        }
        
        // Ensure the path is within the container
        return BlobPath.builder()
                .path(path)
                .container(container)
                .normalizedPath(container.getName() + "/" + normalized)
                .build();
    }
    
    public String getFullPath() {
        return normalizedPath;
    }
    
    public boolean isValid() {
        return path != null && !path.isEmpty() && !normalizedPath.contains("..");
    }
}

