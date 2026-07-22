package com.dudal.javachat.protocol;

public interface ProtocolConnection {
    void connect();
    void disconnect();
    void sendChat(String message) throws Exception;
    void requestCommandSuggestions(String input) throws Exception;
    boolean isConnected();
}
