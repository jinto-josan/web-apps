package com.youtube.common.domain;

import java.util.Objects;

/**
 * Represents a request for paginated data.
 * Contains limit and continuation token for cursor-based pagination.
 */
public class PageRequest {
    private final int limit;
    private final String continuationToken;

    public PageRequest(int limit, String continuationToken) {
        this.limit = limit;
        this.continuationToken = continuationToken;
    }

    public PageRequest(int limit) {
        this(limit, null);
    }

    public int getLimit() {
        return limit;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequest that = (PageRequest) o;
        return limit == that.limit &&
               Objects.equals(continuationToken, that.continuationToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, continuationToken);
    }

    @Override
    public String toString() {
        return "PageRequest{limit=" + limit + ", continuationToken='" + continuationToken + "'}";
    }
}
