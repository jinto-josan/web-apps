package com.youtube.userprofileservice.domain.services;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Service for processing uploaded images (virus scanning and compression).
 */
public interface ImageProcessingService {
    
    /**
     * Scans an image for viruses and malware.
     * 
     * @param imageStream the image input stream
     * @param blobName the blob name for logging
     * @return true if the image is safe, false if threats are detected
     * @throws ImageProcessingException if scanning fails
     */
    boolean scanForViruses(InputStream imageStream, String blobName) throws ImageProcessingException;
    
    /**
     * Compresses an image to reduce file size while maintaining quality.
     * 
     * @param inputStream the original image input stream
     * @param outputStream the output stream for the compressed image
     * @param contentType the content type of the image
     * @param maxWidth maximum width in pixels (maintains aspect ratio)
     * @param maxHeight maximum height in pixels (maintains aspect ratio)
     * @param quality compression quality (0.0 to 1.0, where 1.0 is highest quality)
     * @return the size of the compressed image in bytes
     * @throws ImageProcessingException if compression fails
     */
    long compressImage(InputStream inputStream, OutputStream outputStream, 
                      String contentType, int maxWidth, int maxHeight, 
                      float quality) throws ImageProcessingException;
    
    /**
     * Exception thrown when image processing fails.
     */
    class ImageProcessingException extends Exception {
        public ImageProcessingException(String message) {
            super(message);
        }
        
        public ImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


