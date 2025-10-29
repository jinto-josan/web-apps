package com.youtube.captionssubtitlesservice.domain.valueobjects;

/**
 * Caption file format value object
 */
public enum CaptionFormat {
    SRT,      // SubRip subtitle format
    WebVTT,   // Web Video Text Tracks format
    DFXP,     // Distribution Format Exchange Profile (TTML)
    SUB,      // MicroDVD subtitle format
    ASS,      // Advanced SubStation Alpha
    VTT;      // Alternative WebVTT
    
    public String getFileExtension() {
        return switch (this) {
            case WebVTT, VTT -> ".vtt";
            case SRT -> ".srt";
            case DFXP -> ".dfxp";
            case SUB -> ".sub";
            case ASS -> ".ass";
        };
    }
    
    public String getMimeType() {
        return switch (this) {
            case WebVTT, VTT -> "text/vtt";
            case SRT -> "text/plain";
            case DFXP -> "application/xml+dfxp";
            case SUB -> "text/plain";
            case ASS -> "text/x-ass";
        };
    }
}
