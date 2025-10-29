package com.youtube.livechatservice.domain.valueobjects;

import lombok.NonNull;

import java.util.Objects;

public final class LiveId {
    private final String value;

    private LiveId(String value) {
        this.value = value;
    }

    public static LiveId of(@NonNull String value) {
        return new LiveId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveId liveId = (LiveId) o;
        return Objects.equals(value, liveId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}


