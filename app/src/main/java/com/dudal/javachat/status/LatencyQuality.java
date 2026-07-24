package com.dudal.javachat.status;

public enum LatencyQuality {
    FAST,
    MODERATE,
    SLOW,
    POOR;

    public static LatencyQuality from(long latencyMs) {
        if (latencyMs <= 100) {
            return FAST;
        }
        if (latencyMs <= 200) {
            return MODERATE;
        }
        if (latencyMs <= 350) {
            return SLOW;
        }
        return POOR;
    }
}
