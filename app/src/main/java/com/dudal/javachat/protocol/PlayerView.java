package com.dudal.javachat.protocol;

import java.util.UUID;

public final class PlayerView {
    private final UUID id;
    private final String name;
    private final int latency;
    private final String skinUrl;
    private final boolean showHat;
    private final String profileName;

    public PlayerView(UUID id, String name, int latency) {
        this(id, name, latency, null, true, name);
    }

    public PlayerView(UUID id, String name, int latency, String skinUrl, boolean showHat) {
        this(id, name, latency, skinUrl, showHat, name);
    }

    public PlayerView(UUID id, String name, int latency, String skinUrl, boolean showHat,
                      String profileName) {
        this.id = id;
        this.name = name;
        this.latency = latency;
        this.skinUrl = skinUrl;
        this.showHat = showHat;
        this.profileName = profileName;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getLatency() { return latency; }
    public String getSkinUrl() { return skinUrl; }
    public boolean isShowHat() { return showHat; }
    public String getProfileName() { return profileName; }
}
