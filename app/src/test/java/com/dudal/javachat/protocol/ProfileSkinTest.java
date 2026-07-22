package com.dudal.javachat.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ProfileSkinTest {
    private static final String HASH =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    public void extractsAndUpgradesOfficialTextureUrl() {
        String payload = "{\"textures\":{\"SKIN\":{\"url\":"
                + "\"http://textures.minecraft.net/texture/" + HASH + "\"}}}";
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Player");
        profile.setProperties(List.of(new GameProfile.Property(
                "textures",
                Base64.getEncoder().encodeToString(
                        payload.getBytes(StandardCharsets.UTF_8)))));

        assertEquals("https://textures.minecraft.net/texture/" + HASH,
                ProfileSkin.url(profile));
    }

    @Test
    public void rejectsUntrustedOrMalformedTextureUrls() {
        assertNull(ProfileSkin.safeUrl("https://example.com/texture/" + HASH));
        assertNull(ProfileSkin.safeUrl(
                "https://textures.minecraft.net.evil.example/texture/" + HASH));
        assertNull(ProfileSkin.safeUrl(
                "https://textures.minecraft.net/texture/not-a-hash"));
        assertNull(ProfileSkin.safeUrl(null));
    }
}
