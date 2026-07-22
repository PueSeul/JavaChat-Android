package com.dudal.javachat.status;

public final class ServerStatusResult {
    private final boolean online;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final long latencyMs;
    private final String versionName;

    private ServerStatusResult(boolean online, int onlinePlayers, int maxPlayers,
                               long latencyMs, String versionName) {
        this.online = online;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.latencyMs = latencyMs;
        this.versionName = versionName;
    }

    public static ServerStatusResult online(int onlinePlayers, int maxPlayers,
                                            long latencyMs, String versionName) {
        return new ServerStatusResult(true, onlinePlayers, maxPlayers, latencyMs, versionName);
    }

    public static ServerStatusResult offline() {
        return new ServerStatusResult(false, 0, 0, -1, null);
    }

    public boolean isOnline() { return online; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public long getLatencyMs() { return latencyMs; }
    public String getVersionName() { return versionName; }
}
