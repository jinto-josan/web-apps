package com.youtube.videouploadservice.application.saga;

import com.youtube.videouploadservice.domain.entities.VideoUpload;
import com.youtube.videouploadservice.domain.repositories.VideoUploadRepository;
import com.youtube.videouploadservice.domain.repositories.UploadQuotaRepository;
import com.youtube.videouploadservice.domain.services.BlobStorageService;
import com.youtube.videouploadservice.domain.services.VideoValidator;
import com.youtube.videouploadservice.domain.valueobjects.PreSignedUrl;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

/**
 * Saga for initializing a video upload.
 * Handles validation, quota checking, and pre-signed URL generation.
 * 
 * Saga Steps:
 * 1. Validate upload request
 * 2. Check user quota
 * 3. Generate pre-signed URL
 * 4. Create upload session
 * 5. Return upload metadata
 */
@Slf4j
public class InitializeUploadSaga implements Saga<PreSignedUrl> {
    
    private final String sagaId;
    private final String userId;
    private final String channelId;
    private final String videoTitle;
    private final String videoDescription;
    private final long fileSizeBytes;
    private final String contentType;
    private final int expirationMinutes;
    
    // Dependencies
    private final VideoUploadRepository uploadRepo;
    private final UploadQuotaRepository quotaRepo;
    private final BlobStorageService blobService;
    private final VideoValidator validator;
    
    public InitializeUploadSaga(String sagaId, String userId, String channelId,
                                String videoTitle, String videoDescription,
                                long fileSizeBytes, String contentType, int expirationMinutes,
                                VideoUploadRepository uploadRepo, UploadQuotaRepository quotaRepo,
                                BlobStorageService blobService, VideoValidator validator) {
        this.sagaId = sagaId;
        this.userId = userId;
        this.channelId = channelId;
        this.videoTitle = videoTitle;
        this.videoDescription = videoDescription;
        this.fileSizeBytes = fileSizeBytes;
        this.contentType = contentType;
        this.expirationMinutes = expirationMinutes;
        this.uploadRepo = uploadRepo;
        this.quotaRepo = quotaRepo;
        this.blobService = blobService;
        this.validator = validator;
    }
    
    @Override
    public PreSignedUrl execute() throws SagaExecutionException {
        SagaContext context = new SagaContext(sagaId, getSagaType());
        context.put("userId", userId);
        context.put("channelId", channelId);
        context.put("fileSizeBytes", fileSizeBytes);
        context.put("contentType", contentType);
        context.put("expirationMinutes", expirationMinutes);
        context.put("videoTitle", videoTitle);
        context.put("videoDescription", videoDescription);
        
        try {
            // Step 1: Validate request
            ValidateRequestStep validateStep = new ValidateRequestStep();
            validateStep.execute(context);
            
            // Step 2: Check quota
            CheckQuotaStep quotaStep = new CheckQuotaStep();
            quotaStep.execute(context);
            
            // Step 3: Generate pre-signed URL
            GeneratePreSignedUrlStep urlStep = new GeneratePreSignedUrlStep();
            urlStep.execute(context);
            
            // Step 4: Create upload session
            CreateUploadSessionStep sessionStep = new CreateUploadSessionStep();
            sessionStep.execute(context);
            
            // Return pre-signed URL
            return context.get("preSignedUrl", PreSignedUrl.class);
            
        } catch (SagaStepException e) {
            // Compensate executed steps
            compensate(context, e.getStepName());
            throw new SagaExecutionException(sagaId, getSagaType(), e.getStepName(),
                "Saga execution failed at step: " + e.getStepName(), e);
        }
    }
    
    private void compensate(SagaContext context, String failedStep) {
        log.warn("Compensating saga {} at failed step: {}", sagaId, failedStep);
        
        try {
            // Rollback quota consumption if upload session was created
            if ("CREATE_UPLOAD_SESSION".equals(failedStep)) {
                Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
                if (fileSizeBytes != null) {
                    quotaRepo.releaseQuota(userId, fileSizeBytes, com.youtube.videouploadservice.domain.entities.UploadQuota.QuotaType.DAILY);
                }
            }
        } catch (Exception e) {
            log.error("Compensation failed for saga {}: {}", sagaId, e.getMessage(), e);
        }
    }
    
    @Override
    public String getSagaId() {
        return sagaId;
    }
    
    @Override
    public String getSagaType() {
        return "INITIALIZE_UPLOAD";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return List.of(
            new ValidateRequestStep(),
            new CheckQuotaStep(),
            new GeneratePreSignedUrlStep(),
            new CreateUploadSessionStep()
        );
    }
    
    // Inner step classes
    
    private class ValidateRequestStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
            String contentType = context.get("contentType", String.class);
            
            // Validate file size constraints
            if (!VideoValidator.Constraints.isSizeValid(fileSizeBytes)) {
                throw new SagaStepException(getStepName(), sagaId, "INVALID_FILE_SIZE",
                    "File size must be between 1 KB and 256 GB");
            }
            
            // Basic content type validation
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new SagaStepException(getStepName(), sagaId, "INVALID_CONTENT_TYPE",
                    "Content type must be a video format");
            }
            
            log.info("Request validated successfully for user: {}", userId);
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // No compensation needed for validation
        }
        
        @Override
        public String getStepName() {
            return "VALIDATE_REQUEST";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class CheckQuotaStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String userId = context.get("userId", String.class);
            Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
            
            // Check if user has remaining quota
            boolean hasQuota = quotaRepo.hasRemainingQuota(userId, fileSizeBytes, com.youtube.videouploadservice.domain.entities.UploadQuota.QuotaType.DAILY);
            if (!hasQuota) {
                long remaining = quotaRepo.getRemainingQuota(userId, com.youtube.videouploadservice.domain.entities.UploadQuota.QuotaType.DAILY);
                throw new SagaStepException(getStepName(), sagaId, "QUOTA_EXCEEDED",
                    "Insufficient quota. Remaining: " + remaining + " bytes");
            }
            
            // Consume quota
            quotaRepo.consumeQuota(userId, fileSizeBytes, com.youtube.videouploadservice.domain.entities.UploadQuota.QuotaType.DAILY);
            
            log.info("Quota checked and consumed for user: {}, size: {}", userId, fileSizeBytes);
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // Release consumed quota
            String userId = context.get("userId", String.class);
            Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
            quotaRepo.releaseQuota(userId, fileSizeBytes, com.youtube.videouploadservice.domain.entities.UploadQuota.QuotaType.DAILY);
        }
        
        @Override
        public String getStepName() {
            return "CHECK_QUOTA";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class GeneratePreSignedUrlStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String userId = context.get("userId", String.class);
            Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
            String contentType = context.get("contentType", String.class);
            Integer expirationMinutes = context.get("expirationMinutes", Integer.class);
            
            // Generate blob name
            String blobName = "uploads/" + userId + "/" + sagaId + ".mp4";
            String containerName = "video-uploads";
            
            // Calculate expiration
            Instant expiresAt = Instant.now().plusSeconds(expirationMinutes * 60L);
            
            // Generate pre-signed URL
            PreSignedUrl preSignedUrl = blobService.generatePreSignedUrl(
                containerName,
                blobName,
                userId,
                expiresAt,
                fileSizeBytes,
                contentType
            );
            
            context.put("preSignedUrl", preSignedUrl);
            context.put("blobName", blobName);
            context.put("containerName", containerName);
            
            log.info("Generated pre-signed URL for user: {}, upload: {}", userId, sagaId);
            return preSignedUrl;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // No compensation needed for URL generation
        }
        
        @Override
        public String getStepName() {
            return "GENERATE_PRE_SIGNED_URL";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class CreateUploadSessionStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String userId = context.get("userId", String.class);
            String channelId = context.get("channelId", String.class);
            String videoTitle = context.get("videoTitle", String.class);
            String videoDescription = context.get("videoDescription", String.class);
            Long fileSizeBytes = context.get("fileSizeBytes", Long.class);
            String contentType = context.get("contentType", String.class);
            PreSignedUrl preSignedUrl = context.get("preSignedUrl", PreSignedUrl.class);
            String blobName = context.get("blobName", String.class);
            String containerName = context.get("containerName", String.class);
            Integer expirationMinutes = context.get("expirationMinutes", Integer.class);
            
            Instant now = Instant.now();
            
            // Create upload session
            VideoUpload upload = VideoUpload.builder()
                .id(sagaId)
                .userId(userId)
                .channelId(channelId)
                .videoTitle(videoTitle)
                .videoDescription(videoDescription)
                .status(VideoUpload.UploadStatus.INITIALIZING)
                .totalSizeBytes(fileSizeBytes)
                .uploadedSizeBytes(0L)
                .blobName(blobName)
                .blobContainer(containerName)
                .contentType(contentType)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(preSignedUrl.getExpiresAt())
                .expirationMinutes(expirationMinutes)
                .retryCount(0)
                .maxRetries(3)
                .build();
            
            VideoUpload saved = uploadRepo.save(upload);
            context.put("videoUpload", saved);
            
            log.info("Created upload session: {} for user: {}", sagaId, userId);
            return saved;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // Delete upload session
            uploadRepo.delete(sagaId);
        }
        
        @Override
        public String getStepName() {
            return "CREATE_UPLOAD_SESSION";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
}

