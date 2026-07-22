package com.dudal.javachat.auth;

import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.executor.RequestExecutor;
import net.lenni0451.commons.httpclient.requests.HttpRequest;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DnsRetryingRequestExecutorTest {
    @Test
    public void retriesUnknownHostWithoutReplacingTheRequest() throws Exception {
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest(
                "GET", "https://login.microsoftonline.com/test");
        FlakyExecutor delegate = new FlakyExecutor(client, 2);
        List<Integer> retries = new ArrayList<>();
        List<Long> delays = new ArrayList<>();
        DnsRetryingRequestExecutor executor = new DnsRetryingRequestExecutor(
                client, delegate,
                (host, retry, max) -> retries.add(retry), delays::add);

        HttpResponse response = executor.execute(request);

        assertEquals(200, response.getStatusCode());
        assertEquals(3, delegate.calls);
        assertEquals(List.of(1, 2), retries);
        assertEquals(List.of(500L, 1_500L), delays);
    }

    @Test
    public void reportsHostAndRetryCountAfterRetryLimit() throws Exception {
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest(
                "GET", "https://api.minecraftservices.com/test");
        FlakyExecutor delegate = new FlakyExecutor(client, Integer.MAX_VALUE);
        DnsRetryingRequestExecutor executor = new DnsRetryingRequestExecutor(
                client, delegate, (host, retry, max) -> { }, delay -> { });

        AuthDnsException error = assertThrows(
                AuthDnsException.class, () -> executor.execute(request));

        assertEquals("api.minecraftservices.com", error.getHost());
        assertEquals(3, error.getRetryCount());
        assertEquals(4, delegate.calls);
    }

    @Test
    public void retriesTransientConnectionAbort() throws Exception {
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest(
                "POST", "https://login.microsoftonline.com/test");
        int[] calls = {0};
        RequestExecutor delegate = new RequestExecutor(client) {
            @Override
            public HttpResponse execute(HttpRequest ignored) throws IOException {
                calls[0]++;
                if (calls[0] == 1) {
                    throw new SocketException("Software caused connection abort");
                }
                return new HttpResponse(
                        request.getURL(), 200, new byte[0], Map.of());
            }
        };
        DnsRetryingRequestExecutor executor = new DnsRetryingRequestExecutor(
                client, delegate, (host, retry, max) -> { }, delay -> { });

        assertEquals(200, executor.execute(request).getStatusCode());
        assertEquals(2, calls[0]);
    }

    private static final class FlakyExecutor extends RequestExecutor {
        private final int failures;
        private int calls;

        private FlakyExecutor(HttpClient client, int failures) {
            super(client);
            this.failures = failures;
        }

        @Override
        public HttpResponse execute(HttpRequest request) throws IOException {
            calls++;
            if (calls <= failures) {
                throw new UnknownHostException(request.getURL().getHost());
            }
            return new HttpResponse(
                    request.getURL(), 200, new byte[0], Map.of());
        }
    }
}
