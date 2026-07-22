package com.dudal.javachat.auth;

import java.net.UnknownHostException;

final class AuthDnsException extends UnknownHostException {
    private final String host;
    private final int retryCount;

    AuthDnsException(String host, int retryCount, UnknownHostException cause) {
        super("Unable to resolve " + AuthEndpoint.normalize(host)
                + " after " + retryCount + " retries");
        this.host = AuthEndpoint.normalize(host);
        this.retryCount = retryCount;
        initCause(cause);
    }

    String getHost() {
        return host;
    }

    int getRetryCount() {
        return retryCount;
    }
}
