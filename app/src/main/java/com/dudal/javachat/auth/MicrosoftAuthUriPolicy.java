package com.dudal.javachat.auth;

import java.net.IDN;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/** Limits top-level navigation in the Microsoft sign-in browser. */
public final class MicrosoftAuthUriPolicy {
    private static final List<String> TRUSTED_HOSTS = List.of(
            "microsoft.com",
            "microsoftonline.com",
            "live.com",
            "xbox.com",
            "xboxlive.com"
    );

    private MicrosoftAuthUriPolicy() {}

    public static boolean isTrusted(String rawUri) {
        if (rawUri == null || rawUri.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(rawUri);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                return false;
            }
            String host = IDN.toASCII(uri.getHost()).toLowerCase(Locale.ROOT);
            for (String trusted : TRUSTED_HOSTS) {
                if (host.equals(trusted) || host.endsWith("." + trusted)) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException error) {
            return false;
        }
    }
}
