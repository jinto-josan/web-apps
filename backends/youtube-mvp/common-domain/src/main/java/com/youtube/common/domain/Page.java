package com.youtube.common.domain;

import java.util.List;
import java.util.Objects;

/**
 * Represents a page of data with pagination information.
 * Contains items, continuation token, and indication of whether more data is available.
 * 
 * @param <T> the type of items in the page
 */
public class Page<T> {
    private final List<T> items;
    private final String continuationToken;
    private final boolean hasMore;

    public Page(List<T> items, String continuationToken, boolean hasMore) {
        this.items = Objects.requireNonNull(items, "Items cannot be null");
        this.continuationToken = continuationToken;
        this.hasMore = hasMore;
    }

    public List<T> getItems() {
        return items;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return hasMore == page.hasMore &&
               Objects.equals(items, page.items) &&
               Objects.equals(continuationToken, page.continuationToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, continuationToken, hasMore);
    }

    @Override
    public String toString() {
        return "Page{items=" + items.size() + ", continuationToken='" + continuationToken + 
               "', hasMore=" + hasMore + "}";
    }
}
