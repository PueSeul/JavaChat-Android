package com.dudal.javachat.protocol;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.texture.Texture;
import org.geysermc.mcprotocollib.auth.texture.TextureType;

import java.net.URI;
import java.util.Locale;

public final class ProfileSkin {
    private static final String TEXTURE_HOST = "textures.minecraft.net";

    private ProfileSkin() {
    }

    static String url(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        try {
            Texture skin = profile.getTexture(TextureType.SKIN, false);
            return skin == null ? null : safeUrl(skin.getURL());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /** Only allow Mojang's texture CDN and upgrade its legacy HTTP URLs. */
    public static String safeUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getRawPath();
            if (scheme == null || host == null || path == null
                    || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))
                    || !host.toLowerCase(Locale.ROOT).equals(TEXTURE_HOST)
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || !path.matches("/texture/[0-9a-fA-F]{32,128}")) {
                return null;
            }
            return "https://" + TEXTURE_HOST + path;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
