package com.youtube.mvp.videocatalog.presentation.rest;

import com.youtube.mvp.videocatalog.application.dto.*;
import com.youtube.mvp.videocatalog.application.service.VideoCommandService;
import com.youtube.mvp.videocatalog.application.service.VideoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Video REST controller with API versioning.
 */
@RestController
@RequestMapping("/api/v1/videos")
@Tag(name = "Videos", description = "Video catalog API")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    
    private final VideoCommandService commandService;
    private final VideoQueryService queryService;
    
    @PostMapping
    @Operation(summary = "Create a video", description = "Creates a new video in draft state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Video created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<VideoResponse> createVideo(
            @Valid @RequestBody CreateVideoRequest request) {
        log.info("Creating video");
        
        VideoResponse response = commandService.createVideo(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"" + response.getVersion() + "\"");
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a video", description = "Retrieves a video by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video found"),
            @ApiResponse(responseCode = "304", description = "Not modified"),
            @ApiResponse(responseCode = "404", description = "Video not found", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<VideoResponse> getVideo(
            @PathVariable String id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        log.debug("Getting video: {}", id);
        
        VideoResponse response = queryService.getVideo(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"" + response.getVersion() + "\"");
        
        // Check If-None-Match
        if (ifNoneMatch != null && ifNoneMatch.equals("\"" + response.getVersion() + "\"")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_MODIFIED)
                    .headers(headers)
                    .build();
        }
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(response);
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update a video", description = "Updates video metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or version mismatch"),
            @ApiResponse(responseCode = "404", description = "Video not found", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<VideoResponse> updateVideo(
            @PathVariable String id,
            @Valid @RequestBody UpdateVideoRequest request,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        log.info("Updating video: {}", id);
        
        VideoResponse response = commandService.updateVideo(id, request, ifMatch);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"" + response.getVersion() + "\"");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a video", description = "Deletes a video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Video deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Video not found", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Void> deleteVideo(@PathVariable String id) {
        log.info("Deleting video: {}", id);
        
        commandService.deleteVideo(id);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a video", description = "Publishes a video (transitions from draft to published)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video published successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid state for publishing"),
            @ApiResponse(responseCode = "404", description = "Video not found", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<VideoResponse> publishVideo(@PathVariable String id) {
        log.info("Publishing video: {}", id);
        
        VideoResponse response = commandService.publishVideo(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setETag("\"" + response.getVersion() + "\"");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get videos by channel", description = "Retrieves videos for a specific channel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    })
    public ResponseEntity<PagedResponse<VideoResponse>> getVideosByChannel(
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String visibility,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        if (channelId != null) {
            PagedResponse<VideoResponse> response = queryService.getVideosByChannel(channelId, page, size);
            return ResponseEntity.ok(response);
        }
        
        if (state != null) {
            PagedResponse<VideoResponse> response = queryService.getVideosByState(state, page, size);
            return ResponseEntity.ok(response);
        }
        
        if (visibility != null) {
            PagedResponse<VideoResponse> response = queryService.getVideosByVisibility(visibility, page, size);
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.badRequest().build();
    }
}

