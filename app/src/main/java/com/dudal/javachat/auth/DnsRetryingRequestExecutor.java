package com.dudal.javachat.auth;

import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.executor.RequestExecutor;
import net.lenni0451.commons.httpclient.requests.HttpRequest;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

final class DnsRetryingRequestExecutor extends RequestExecutor {
    interface RetryListener {
        void onRetry(String host, int retryNumber, int maxRetries);
    }

    interface Sleeper {
        void sleep(long delayMs) throws InterruptedException;
    }

    static final int MAX_RETRIES = 3;
    private static final long[] RETRY_DELAYS_MS = {500L, 1_500L, 3_000L};

    private final RequestExecutor delegate;
    private final RetryListener retryListener;
    private final Sleeper sleeper;

    DnsRetryingRequestExecutor(HttpClient client, RequestExecutor delegate,
                               RetryListener retryListener) {
        this(client, delegate, retryListener, Thread::sleep);
    }

    DnsRetryingRequestExecutor(HttpClient client, RequestExecutor delegate,
                               RetryListener retryListener, Sleeper sleeper) {
        super(client);
        this.delegate = Objects.requireNonNull(delegate);
        this.retryListener = Objects.requireNonNull(retryListener);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException, InterruptedException {
        int retries = 0;
        while (true) {
            try {
                return delegate.execute(request);
            } catch (UnknownHostException error) {
                if (retries >= MAX_RETRIES) {
                    throw new AuthDnsException(request.getURL().getHost(), retries, error);
                }
                retries++;
                retryListener.onRetry(
                        request.getURL().getHost(), retries, MAX_RETRIES);
                sleeper.sleep(RETRY_DELAYS_MS[retries - 1]);
            } catch (IOException error) {
                if (!isTransientConnectionFailure(error)) {
                    throw error;
                }
                if (retries >= MAX_RETRIES) {
                    throw new AuthConnectionException(
                            request.getURL().getHost(), retries, error);
                }
                retries++;
                retryListener.onRetry(
                        request.getURL().getHost(), retries, MAX_RETRIES);
                sleeper.sleep(RETRY_DELAYS_MS[retries - 1]);
            }
        }
    }

    private static boolean isTransientConnectionFailure(IOException error) {
        return error instanceof SocketException || error instanceof SocketTimeoutException;
    }
}
