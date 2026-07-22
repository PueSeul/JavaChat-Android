package com.dudal.javachat.auth;

import net.raphimc.minecraftauth.java.model.MinecraftPlayerCertificates;

import java.util.UUID;

public final class OnlineIdentity {
    private final UUID profileId;
    private final String profileName;
    private final String accessToken;
    private final MinecraftPlayerCertificates certificates;

    OnlineIdentity(UUID profileId, String profileName, String accessToken,
                   MinecraftPlayerCertificates certificates) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.accessToken = accessToken;
        this.certificates = certificates;
    }

    public UUID getProfileId() { return profileId; }
    public String getProfileName() { return profileName; }
    public String getAccessToken() { return accessToken; }
    public MinecraftPlayerCertificates getCertificates() { return certificates; }
}
