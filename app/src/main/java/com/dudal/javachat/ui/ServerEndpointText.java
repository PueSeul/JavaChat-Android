package com.dudal.javachat.ui;

public final class ServerEndpointText {
    private static final int DEFAULT_MINECRAFT_PORT = 25565;

    private ServerEndpointText() {}

    public static String format(String host, int port) {
        return port == DEFAULT_MINECRAFT_PORT ? host : host + ":" + port;
    }
}
