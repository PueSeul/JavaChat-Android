package com.dudal.javachat.auth;

import com.dudal.javachat.data.ConnectionSettingsRepository;

public final class ConnectionIdentity {
    private final OnlineIdentity onlineIdentity;
    private final String offlineNickname;

    private ConnectionIdentity(OnlineIdentity onlineIdentity, String offlineNickname) {
        this.onlineIdentity = onlineIdentity;
        this.offlineNickname = offlineNickname;
    }

    public static ConnectionIdentity online(OnlineIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Microsoft 계정 로그인이 필요합니다.");
        }
        return new ConnectionIdentity(identity, null);
    }

    public static ConnectionIdentity offline(String nickname) {
        if (!ConnectionSettingsRepository.isValidOfflineNickname(nickname)) {
            throw new IllegalArgumentException(
                    "오프라인 닉네임은 영문, 숫자, 밑줄 3~16자여야 합니다.");
        }
        return new ConnectionIdentity(null, nickname);
    }

    public boolean isOnline() {
        return onlineIdentity != null;
    }

    public OnlineIdentity getOnlineIdentity() {
        return onlineIdentity;
    }

    public String getOfflineNickname() {
        return offlineNickname;
    }
}
