package be.sigfried.weatherstationbackend.Models;

import java.time.Duration;

public enum HistoryEnum {
    hour(null),
    day(Duration.ofMinutes(30)),
    week(Duration.ofHours(12)),
    month(Duration.ofHours(12)),
    year(Duration.ofDays(3));

    private final Duration cacheLength;

    HistoryEnum(Duration duration) {
        this.cacheLength = duration;
    }

    public Duration getCacheLength() {
        return cacheLength;
    }
}
