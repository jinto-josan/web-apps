package com.youtube.commentsservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class ReactionCount {
    private String type;
    private int count;
    private Map<String, String> userReactions; // userId -> timestamp

    public void addUserReaction(String userId) {
        if (!userReactions.containsKey(userId)) {
            userReactions.put(userId, java.time.Instant.now().toString());
            count++;
        }
    }

    public void removeUserReaction(String userId) {
        if (userReactions.remove(userId) != null) {
            count--;
        }
    }

    public boolean hasUserReaction(String userId) {
        return userReactions.containsKey(userId);
    }

    public Set<String> getUserIds() {
        return new HashSet<>(userReactions.keySet());
    }
}

