package com.dudal.javachat.protocol;

public enum ConnectionState {
    DISCONNECTED("연결 안 됨"),
    CONNECTING("서버 연결 중"),
    AUTHENTICATING("로그인 중"),
    CONNECTED("연결됨"),
    ERROR("연결 오류");

    private final String label;

    ConnectionState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
