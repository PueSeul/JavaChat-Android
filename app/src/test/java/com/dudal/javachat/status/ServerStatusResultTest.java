package com.dudal.javachat.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerStatusResultTest {
    @Test
    public void testOnlineAndOfflineResults() {
        ServerStatusResult online = ServerStatusResult.online(3, 20, 42, "26.2");
        assertTrue(online.isOnline());
        assertEquals(3, online.getOnlinePlayers());
        assertEquals(20, online.getMaxPlayers());
        assertEquals(42, online.getLatencyMs());
        assertEquals("26.2", online.getVersionName());

        assertFalse(ServerStatusResult.offline().isOnline());
    }
}
