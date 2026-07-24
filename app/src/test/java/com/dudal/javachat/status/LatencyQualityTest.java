package com.dudal.javachat.status;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LatencyQualityTest {
    @Test
    public void mapsLatencyToFourReadableBands() {
        assertEquals(LatencyQuality.FAST, LatencyQuality.from(100));
        assertEquals(LatencyQuality.MODERATE, LatencyQuality.from(101));
        assertEquals(LatencyQuality.MODERATE, LatencyQuality.from(200));
        assertEquals(LatencyQuality.SLOW, LatencyQuality.from(201));
        assertEquals(LatencyQuality.SLOW, LatencyQuality.from(350));
        assertEquals(LatencyQuality.POOR, LatencyQuality.from(351));
    }
}
