package com.dudal.javachat.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ConnectionSettingsRepository {
    public static final String DEFAULT_OFFLINE_NICKNAME = "MobilePlayer";

    private static final String PREFS = "connection_settings";
    private static final String KEY_AUTH_MODE = "auth_mode";
    private static final String KEY_OFFLINE_NICKNAME = "offline_nickname";
    private static final String LEGACY_SERVER_PREFS = "saved_servers";
    private static final String LEGACY_SERVERS_KEY = "servers_json";

    private final SharedPreferences preferences;

    public ConnectionSettingsRepository(Context context) {
        Context appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!preferences.contains(KEY_AUTH_MODE)) {
            migrateLegacyServerSetting(appContext);
        }
    }

    public AuthMode getAuthMode() {
        return parseAuthMode(preferences.getString(KEY_AUTH_MODE, null));
    }

    public void setAuthMode(AuthMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("접속 방식을 선택해 주세요.");
        }
        preferences.edit().putString(KEY_AUTH_MODE, mode.name()).apply();
    }

    public String getOfflineNickname() {
        String nickname = preferences.getString(
                KEY_OFFLINE_NICKNAME, DEFAULT_OFFLINE_NICKNAME);
        return isValidOfflineNickname(nickname) ? nickname : DEFAULT_OFFLINE_NICKNAME;
    }

    public void setOfflineNickname(String nickname) {
        String normalized = nickname == null ? "" : nickname.trim();
        if (!isValidOfflineNickname(normalized)) {
            throw new IllegalArgumentException(
                    "오프라인 닉네임은 영문, 숫자, 밑줄 3~16자여야 합니다.");
        }
        preferences.edit().putString(KEY_OFFLINE_NICKNAME, normalized).apply();
    }

    public static boolean isValidOfflineNickname(String nickname) {
        return nickname != null && nickname.matches("[A-Za-z0-9_]{3,16}");
    }

    static AuthMode parseAuthMode(String value) {
        try {
            return value == null ? AuthMode.OFFLINE : AuthMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return AuthMode.OFFLINE;
        }
    }

    private void migrateLegacyServerSetting(Context context) {
        AuthMode mode = AuthMode.OFFLINE;
        String nickname = DEFAULT_OFFLINE_NICKNAME;
        try {
            String json = context.getSharedPreferences(
                    LEGACY_SERVER_PREFS, Context.MODE_PRIVATE)
                    .getString(LEGACY_SERVERS_KEY, "[]");
            JsonArray servers = JsonParser.parseString(json).getAsJsonArray();
            if (!servers.isEmpty()) {
                JsonObject first = servers.get(0).getAsJsonObject();
                JsonElement legacyMode = first.get("authMode");
                if (legacyMode != null && !legacyMode.isJsonNull()) {
                    mode = parseAuthMode(legacyMode.getAsString());
                }
                JsonElement legacyNickname = first.get("offlineNickname");
                if (legacyNickname != null && !legacyNickname.isJsonNull()
                        && isValidOfflineNickname(legacyNickname.getAsString())) {
                    nickname = legacyNickname.getAsString();
                }
            }
        } catch (RuntimeException ignored) {
            // Corrupt legacy settings should not prevent the app from starting.
        }
        preferences.edit()
                .putString(KEY_AUTH_MODE, mode.name())
                .putString(KEY_OFFLINE_NICKNAME, nickname)
                .apply();
    }
}
