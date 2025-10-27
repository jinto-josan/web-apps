package com.youtube.channelservice.infrastructure.persistence;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.youtube.channelservice.domain.repositories.HandleRegistry;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class CosmosHandleRegistry implements HandleRegistry {  
    private final CosmosContainer handles;  
  
    public CosmosHandleRegistry(CosmosContainer handles) { this.handles = handles; }  
  
    @Override  
    public boolean reserve(String handleLower, String userId, Duration ttl) {  
        int bucket = bucket(handleLower);  
        HandleDoc doc = new HandleDoc();  
        doc.id = handleLower; doc.bucket = bucket; doc.status = "RESERVED";  
        doc.reservedByUserId = userId; doc.reservedAt = Instant.now().toString();  
        doc.ttl = (int) ttl.getSeconds(); // requires TTL enabled on container  
        try {  
            CosmosItemRequestOptions opts = new CosmosItemRequestOptions().setIfNoneMatch(true);  
            handles.createItem(doc, new PartitionKey(bucket), opts);  
            return true;  
        } catch (CosmosException ex) {  
            if (ex.getStatusCode() == 409) return false;  
            throw ex;  
        }  
    }  
  
    @Override  
    public boolean commit(String handleLower, String channelId) {  
        int bucket = bucket(handleLower);  
        try {  
            CosmosItemResponse<HandleDoc> resp = handles.readItem(handleLower, new PartitionKey(bucket), HandleDoc.class);  
            HandleDoc doc = resp.getItem();  
            if (!"RESERVED".equals(doc.status)) return false;  
            doc.status = "COMMITTED";  
            doc.channelId = channelId;  
            doc.committedAt = Instant.now().toString();  
            CosmosItemRequestOptions opts = new CosmosItemRequestOptions().setIfMatchETag(resp.getResponseHeaders().get("ETag"));  
            handles.replaceItem(doc, doc.id, new PartitionKey(bucket), opts);  
            return true;  
        } catch (CosmosException ex) {  
            if (ex.getStatusCode() == 404) return false;  
            throw ex;  
        }  
    }  
  
    @Override  
    public Optional<String> lookupChannelId(String handleLower) {  
        int bucket = bucket(handleLower);  
        try {  
            CosmosItemResponse<HandleDoc> resp = handles.readItem(handleLower, new PartitionKey(bucket), HandleDoc.class);  
            HandleDoc doc = resp.getItem();  
            return Optional.ofNullable(doc.channelId);  
        } catch (CosmosException ex) {  
            if (ex.getStatusCode() == 404) return Optional.empty();  
            throw ex;  
        }  
    }  
  
    @Override  
    public boolean release(String handleLower) {  
        int bucket = bucket(handleLower);  
        try {  
            handles.deleteItem(handleLower, new PartitionKey(bucket), new CosmosItemRequestOptions());  
            return true;  
        } catch (CosmosException ex) {  
            if (ex.getStatusCode() == 404) return false;  
            throw ex;  
        }  
    }  
  
    private int bucket(String handleLower) {  
        try {  
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  
            byte[] hash = digest.digest(handleLower.getBytes(java.nio.charset.StandardCharsets.UTF_8));  
            int v = ByteBuffer.wrap(hash, 0, 4).getInt() & 0x7FFFFFFF;  
            return v % 128;  
        } catch (Exception e) { throw new RuntimeException(e); }  
    }  
  
    static class HandleDoc {  
        public String id;  
        public int bucket;  
        public String channelId;  
        public String status;  
        public String reservedByUserId;  
        public String reservedAt;  
        public String committedAt;  
        public Integer ttl; // TTL seconds  
    }  
}  