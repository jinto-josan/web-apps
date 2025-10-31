package com.youtube.observabilityservice.domain.valueobjects;

import lombok.Value;

import java.time.Duration;

@Value
public class TimeWindow {
    Duration duration;
    TimeWindowType type;

    public enum TimeWindowType {
        ROLLING,
        CALENDAR
    }

    public static TimeWindow rollingDays(int days) {
        return new TimeWindow(Duration.ofDays(days), TimeWindowType.ROLLING);
    }

    public static TimeWindow rollingHours(int hours) {
        return new TimeWindow(Duration.ofHours(hours), TimeWindowType.ROLLING);
    }

    public static TimeWindow calendarDays(int days) {
        return new TimeWindow(Duration.ofDays(days), TimeWindowType.CALENDAR);
    }
}

