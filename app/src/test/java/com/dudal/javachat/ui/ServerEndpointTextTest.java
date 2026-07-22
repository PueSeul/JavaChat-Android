package com.dudal.javachat.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class ServerEndpointTextTest {
    @Test
    public void hidesDefaultMinecraftPort() {
        assertEquals("play.example.com",
                ServerEndpointText.format("play.example.com", 25565));
    }

    @Test
    public void keepsNonDefaultPort() {
        assertEquals("play.example.com:25566",
                ServerEndpointText.format("play.example.com", 25566));
    }
}
