package com.dudal.javachat.auth;

import java.io.IOException;

final class AuthConnectionException extends IOException {
    private final String host;
    private final int retryCount;

    AuthConnectionException(String host, int retryCount, IOException cause) {
        super("Connection to " + AuthEndpoint.normalize(host)
                + " failed after " + retryCount + " retries", cause);
        this.host = AuthEndpoint.normalize(host);
        this.retryCount = retryCount;
    }

    String getHost() {
        return host;
    }

    int getRetryCount() {
        return retryCount;
    }
}
