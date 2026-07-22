package com.dudal.javachat.auth;

import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;

import java.util.Objects;
import java.util.function.Consumer;

final class AuthHttpClientFactory {
    private AuthHttpClientFactory() {
    }

    static HttpClient create(String userAgent, Consumer<String> statusConsumer) {
        Consumer<String> safeStatusConsumer = Objects.requireNonNull(statusConsumer);
        AuthDns dns = new AuthDns(host -> safeAccept(safeStatusConsumer,
                AuthEndpoint.label(host) + " 서버에 호환 DNS로 연결 중…"));
        HttpClient httpClient = MinecraftAuth.createHttpClient(userAgent);
        httpClient.setExecutor(client -> {
            OkHttpAuthExecutor executor = new OkHttpAuthExecutor(client, dns);
            return new DnsRetryingRequestExecutor(client, executor,
                    (host, retryNumber, maxRetries) -> safeAccept(safeStatusConsumer,
                            AuthEndpoint.label(host) + " 서버 재연결 중… ("
                                    + retryNumber + "/" + maxRetries + ")"));
        });
        return httpClient;
    }

    private static void safeAccept(Consumer<String> consumer, String status) {
        try {
            consumer.accept(status);
        } catch (RuntimeException ignored) {
            // A UI status listener must not interrupt authentication.
        }
    }
}
