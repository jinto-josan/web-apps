package com.youtube.channelservice.interfaces.rest;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.channelservice.application.commands.*;
import com.youtube.channelservice.application.usecases.ChannelUseCase;
import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.models.Branding;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelUseCase channelUseCase;
    private final CommandFactory commandFactory;

    public ChannelController(ChannelUseCase channelUseCase, CommandFactory commandFactory) {
        this.channelUseCase = channelUseCase;
        this.commandFactory = commandFactory;
    }

    @PostMapping
    public ResponseEntity<Channel> create(@AuthenticationPrincipal Jwt jwt,
                                          @RequestBody CreateChannelRequest req) {
        String ownerUserId = JwtUser.userId(jwt);
        String ulid = UlidCreator.getUlid().toString();
        String commandId = UUID.randomUUID().toString();
        
        CreateChannelCommand command = commandFactory.createChannelCommand(
            ownerUserId, req.handle, req.title, req.description, req.language, req.country
        );
        
        Channel channel = channelUseCase.createChannel(command);
        return ResponseEntity.status(201).body(channel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> get(@PathVariable String id) {
        // This would need a query handler implementation
        // For now, returning a placeholder
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/by-handle/{handle}")
    public ResponseEntity<Channel> byHandle(@PathVariable String handle) {
        // This would need a query handler implementation
        // For now, returning a placeholder
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/handle")
    public ResponseEntity<Channel> changeHandle(@PathVariable String id,
                                                @AuthenticationPrincipal Jwt jwt,
                                                @RequestHeader("If-Match") String etag,
                                                @RequestBody ChangeHandleRequest req) {
        String actorUserId = JwtUser.userId(jwt);
        String commandId = UUID.randomUUID().toString();
        
        ChangeHandleCommand command = commandFactory.changeHandleCommand(
            id, actorUserId, req.newHandle, etag,
            req.lastChangedAt == null ? null : java.time.Instant.parse(req.lastChangedAt)
        );
        
        Channel updated = channelUseCase.changeHandle(command);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/branding")
    public ResponseEntity<Channel> updateBranding(@PathVariable String id,
                                                  @AuthenticationPrincipal Jwt jwt,
                                                  @RequestHeader("If-Match") String etag,
                                                  @RequestBody BrandingRequest req) {
        String actorUserId = JwtUser.userId(jwt);
        String commandId = UUID.randomUUID().toString();
        
        Branding branding = new Branding(req.avatarUri, req.bannerUri, req.accentColor);
        UpdateBrandingCommand command = commandFactory.updateBrandingCommand(
            id, actorUserId, branding, etag
        );
        
        Channel updated = channelUseCase.updateBranding(command);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable String id,
                                          @AuthenticationPrincipal Jwt jwt,
                                          @RequestBody MemberRequest req) {
        String actorUserId = JwtUser.userId(jwt);
        // This would need a separate command for adding members
        // For now, using setMemberRole as a workaround
        String commandId = UUID.randomUUID().toString();
        
        SetMemberRoleCommand command = commandFactory.setMemberRoleCommand(
            id, actorUserId, req.userId, Role.valueOf(req.role)
        );
        
        channelUseCase.setMemberRole(command);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable String id,
                                             @PathVariable String userId,
                                             @AuthenticationPrincipal Jwt jwt) {
        String actorUserId = JwtUser.userId(jwt);
        // This would need a separate command for removing members
        // For now, returning not implemented
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/members/{userId}/role")
    public ResponseEntity<Void> setRole(@PathVariable String id,
                                        @PathVariable String userId,
                                        @AuthenticationPrincipal Jwt jwt,
                                        @RequestBody RoleRequest req) {
        String actorUserId = JwtUser.userId(jwt);
        String commandId = UUID.randomUUID().toString();
        
        SetMemberRoleCommand command = commandFactory.setMemberRoleCommand(
            id, actorUserId, userId, Role.valueOf(req.role)
        );
        
        channelUseCase.setMemberRole(command);
        return ResponseEntity.noContent().build();
    }

    // DTOs
    static class CreateChannelRequest { public String handle, title, description, language, country; }
    static class ChangeHandleRequest { public String newHandle; public String lastChangedAt; }
    static class BrandingRequest { public String avatarUri, bannerUri, accentColor; }
    static class MemberRequest { public String userId, role; }
    static class RoleRequest { public String role; }

    // Helper to extract user id from JWT
    final class JwtUser {
        static String userId(Jwt jwt) {
            String uid = jwt.getClaimAsString("uid");
            return (uid != null && !uid.isBlank()) ? uid : jwt.getSubject();
        }
    }
}

