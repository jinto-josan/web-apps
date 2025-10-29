package com.youtube.captionssubtitlesservice.interfaces.rest;

import com.youtube.captionssubtitlesservice.application.usecases.*;
import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Captions", description = "Captions and subtitles management API")
public class CaptionController {
    
    private final CreateCaptionUseCase createCaptionUseCase;
    private final AutoGenerateCaptionUseCase autoGenerateCaptionUseCase;
    private final TranslateCaptionUseCase translateCaptionUseCase;
    private final GetCaptionUseCase getCaptionUseCase;
    
    @Operation(summary = "List all captions for a video")
    @GetMapping("/videos/{videoId}/captions")
    public ResponseEntity<List<Caption>> listCaptions(@PathVariable String videoId) {
        List<Caption> captions = getCaptionUseCase.listByVideoId(videoId);
        return ResponseEntity.ok(captions);
    }
    
    @Operation(summary = "Get caption by ID")
    @GetMapping("/captions/{captionId}")
    public ResponseEntity<Caption> getCaption(
            @PathVariable String captionId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        
        Caption caption = getCaptionUseCase.getById(captionId);
        
        // ETag support
        String etag = caption.getEtag();
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"" + etag + "\"");
        
        return ResponseEntity.ok().headers(headers).body(caption);
    }
    
    @Operation(summary = "Download caption content")
    @GetMapping("/captions/{captionId}/content")
    public ResponseEntity<String> getCaptionContent(@PathVariable String captionId) {
        Caption caption = getCaptionUseCase.getById(captionId);
        String content = getCaptionUseCase.getCaptionContent(caption.getBlobUri());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(caption.getFormat().getMimeType()));
        
        return ResponseEntity.ok().headers(headers).body(content);
    }
    
    @Operation(summary = "Auto-generate captions using STT")
    @PostMapping("/videos/{videoId}/captions/auto")
    public ResponseEntity<Caption> autoGenerateCaption(
            @PathVariable String videoId,
            @RequestParam @NotBlank String audioUri,
            @RequestParam(defaultValue = "en") String language) {
        
        LanguageCode langCode = LanguageCode.fromCode(language);
        Caption caption = autoGenerateCaptionUseCase.execute(videoId, audioUri, langCode);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(caption);
    }
    
    @Operation(summary = "Translate captions to another language")
    @PostMapping("/captions/{captionId}/translate")
    public ResponseEntity<Caption> translateCaption(
            @PathVariable String captionId,
            @RequestParam @NotBlank String targetLanguage) {
        
        Caption caption = translateCaptionUseCase.execute(captionId, targetLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(caption);
    }
    
    @Operation(summary = "Upload manual captions")
    @PostMapping("/videos/{videoId}/captions")
    public ResponseEntity<Caption> uploadCaption(
            @PathVariable String videoId,
            @RequestParam @NotBlank String language,
            @RequestParam(defaultValue = "WebVTT") String format,
            @RequestParam InputStream content,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getClaim("sub");
        LanguageCode langCode = LanguageCode.fromCode(language);
        com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat fmt = 
            com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat.valueOf(format);
        
        Caption caption = createCaptionUseCase.execute(videoId, langCode, fmt, content, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(caption);
    }
    
    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
