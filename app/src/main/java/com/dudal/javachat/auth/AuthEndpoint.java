package com.dudal.javachat.auth;

import java.util.Locale;
import java.util.Set;

final class AuthEndpoint {
    private static final Set<String> COMPATIBLE_DNS_HOSTS = Set.of(
            "login.microsoftonline.com",
            "login.live.com",
            "device.auth.xboxlive.com",
            "sisu.xboxlive.com",
            "user.auth.xboxlive.com",
            "xsts.auth.xboxlive.com",
            "api.minecraftservices.com"
    );

    private AuthEndpoint() {
    }

    static boolean supportsCompatibleDns(String host) {
        return COMPATIBLE_DNS_HOSTS.contains(normalize(host));
    }

    static String label(String host) {
        return switch (normalize(host)) {
            case "login.microsoftonline.com", "login.live.com" -> "Microsoft 계정 로그인";
            case "device.auth.xboxlive.com", "sisu.xboxlive.com",
                    "user.auth.xboxlive.com" -> "Xbox 계정 인증";
            case "xsts.auth.xboxlive.com" -> "Xbox 보안 토큰 인증";
            case "api.minecraftservices.com" -> "Minecraft 프로필 인증";
            default -> "Microsoft 인증";
        };
    }

    static String normalize(String host) {
        if (host == null) {
            return "";
        }
        String normalized = host.trim().toLowerCase(Locale.ROOT);
        return normalized.endsWith(".")
                ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
