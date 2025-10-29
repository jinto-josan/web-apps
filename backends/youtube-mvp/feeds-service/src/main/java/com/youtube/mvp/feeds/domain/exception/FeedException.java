package com.youtube.mvp.feeds.domain.exception;

public class FeedException extends RuntimeException {
    public FeedException(String message) {
        super(message);
    }

    public FeedException(String message, Throwable cause) {
        super(message, cause);
    }
}

