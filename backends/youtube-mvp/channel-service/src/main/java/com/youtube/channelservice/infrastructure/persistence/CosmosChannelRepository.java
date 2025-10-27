package com.youtube.channelservice.infrastructure.persistence;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.repositories.ChannelRepository;

import java.time.Instant;
import java.util.Optional;

public class CosmosChannelRepository implements ChannelRepository {  
    private final CosmosContainer channels;  
  
    public CosmosChannelRepository(CosmosContainer channels) {  
        this.channels = channels;  
    }  
  
    @Override  
    public Optional<Channel> findById(String id) {  
        try {  
            CosmosItemResponse<ChannelDoc> resp = channels.readItem(id, new PartitionKey(id), ChannelDoc.class);  
            return Optional.of(toDomain(resp.getItem(), resp.getResponseHeaders().get("ETag")));  
        } catch (CosmosException ex) {  
            if (ex.getStatusCode() == 404) return Optional.empty();  
            throw ex;  
        }  
    }  
  
    @Override  
    public Channel saveNew(Channel c) {  
        ChannelDoc doc = toDoc(c);  
        CosmosItemResponse<ChannelDoc> resp = channels.createItem(doc, new PartitionKey(doc.id), new CosmosItemRequestOptions());  
        return toDomain(resp.getItem(), resp.getResponseHeaders().get("ETag"));  
    }  
  
    @Override  
    public void delete(String id) {  
        channels.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());  
    }  
  
    @Override  
    public Channel updateHandle(String channelId, String oldHandle, String newHandle, String ifMatchEtag, int newVersion, Instant now) {  
        // Read latest doc; then replace with If-Match  
        ChannelDoc doc = channels.readItem(channelId, new PartitionKey(channelId), ChannelDoc.class).getItem();  
        if (!doc.handleLower.equals(oldHandle)) throw new IllegalStateException("STALE_HANDLE");  
        doc.handleLower = newHandle;  
        doc.version = newVersion;  
        doc.timestamps.updatedAt = now.toString();  
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions().setIfMatchETag(ifMatchEtag);  
        CosmosItemResponse<ChannelDoc> resp = channels.replaceItem(doc, channelId, new PartitionKey(channelId), opts);  
        return toDomain(resp.getItem(), resp.getResponseHeaders().get("ETag"));  
    }  
  
    @Override
    public Channel updateBranding(Channel existing, Branding branding, String ifMatchEtag) {
        ChannelDoc doc = channels.readItem(existing.id(), new PartitionKey(existing.id()), ChannelDoc.class).getItem();
        doc.branding = new BrandingDoc(branding.avatarUri(), branding.bannerUri(), branding.accentColor());
        doc.version = existing.version() + 1;
        doc.timestamps.updatedAt = Instant.now().toString();
        CosmosItemResponse<ChannelDoc> resp = channels.replaceItem(doc, existing.id(), new PartitionKey(existing.id()),
                new CosmosItemRequestOptions().setIfMatchETag(ifMatchEtag));
        return toDomain(resp.getItem(), resp.getResponseHeaders().get("ETag"));
    }
  
    private Channel toDomain(ChannelDoc d, String etag) {  
        return Channel.builder()  
                .id(d.id).ownerUserId(d.ownerUserId).handleLower(d.handleLower)  
                .title(d.title).description(d.description)  
                .language(d.language).country(d.country)  
                .branding(d.branding == null ? null : new Branding(d.branding.avatarUri, d.branding.bannerUri, d.branding.accentColor))
                .policy(d.policy == null ? null : new Policy(d.policy.ageGate, d.policy.regionBlocks))
                .version(d.version)  
                .createdAt(Instant.parse(d.timestamps.createdAt))  
                .updatedAt(Instant.parse(d.timestamps.updatedAt))  
                .etag(etag)  
                .build();  
    }  
  
    private ChannelDoc toDoc(Channel c) {  
        ChannelDoc d = new ChannelDoc();  
        d.id = c.id(); d.ownerUserId = c.ownerUserId(); d.handleLower = c.handleLower();  
        d.title = c.title(); d.description = c.description();  
        d.language = c.language(); d.country = c.country();  
        d.branding = c.branding() == null ? null : new BrandingDoc(c.branding().avatarUri(), c.branding().bannerUri(), c.branding().accentColor());  
        d.policy = c.policy() == null ? null : PolicyDoc.of(c.policy());  
        d.version = c.version();  
        d.timestamps = new TimestampsDoc(Instant.now().toString(), Instant.now().toString());  
        return d;  
    }  
  
    // DTOs mapped to Cosmos documents  
    static class ChannelDoc {  
        public String id;  
        public String ownerUserId;  
        public String handleLower;  
        public String title;  
        public String description;  
        public String language;  
        public String country;  
        public BrandingDoc branding;  
        public PolicyDoc policy;  
        public FeaturesDoc features;  
        public StatsDoc stats;  
        public TimestampsDoc timestamps;  
        public int version;  
    }  
    static class BrandingDoc { public String avatarUri, bannerUri, accentColor; BrandingDoc(){} BrandingDoc(String a,String b,String c){avatarUri=a;bannerUri=b;accentColor=c;} }  
    static class PolicyDoc { public boolean ageGate; public java.util.List<String> regionBlocks;  
        static PolicyDoc of(com.example.channel.domain.Policy p){ PolicyDoc d = new PolicyDoc(); d.ageGate=p.ageGate(); d.regionBlocks=p.regionBlocks(); return d; }  
    }  
    static class FeaturesDoc { public boolean monetizationEnabled; public boolean communityEnabled; }  
    static class StatsDoc { public long subscribers; }  
    static class TimestampsDoc { public String createdAt, updatedAt; TimestampsDoc(){} TimestampsDoc(String c,String u){createdAt=c;updatedAt=u;} }  
} 
