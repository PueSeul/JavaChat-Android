package com.dudal.javachat.auth;

import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.HttpResponse;
import net.lenni0451.commons.httpclient.content.HttpContent;
import net.lenni0451.commons.httpclient.executor.RequestExecutor;
import net.lenni0451.commons.httpclient.requests.HttpContentRequest;
import net.lenni0451.commons.httpclient.requests.HttpRequest;

import java.io.IOException;
import java.net.CookieManager;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class OkHttpAuthExecutor extends RequestExecutor {
    private final OkHttpClient baseClient;

    OkHttpAuthExecutor(HttpClient client, AuthDns dns) {
        super(client);
        baseClient = new OkHttpClient.Builder()
                .dns(dns)
                .connectTimeout(client.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(client.getReadTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(client.isFollowRedirects())
                .followSslRedirects(client.isFollowRedirects())
                .retryOnConnectionFailure(false)
                .build();
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException {
        CookieManager cookieManager = getCookieManager(request);
        Request.Builder builder = new Request.Builder().url(request.getURL());
        addHeaders(builder, getHeaders(request, cookieManager));
        builder.method(request.getMethod(), requestBody(request));

        boolean followRedirects = switch (request.getFollowRedirects()) {
            case FOLLOW -> true;
            case IGNORE -> false;
            case NOT_SET -> client.isFollowRedirects();
        };
        OkHttpClient requestClient = baseClient.followRedirects() == followRedirects
                ? baseClient
                : baseClient.newBuilder()
                        .followRedirects(followRedirects)
                        .followSslRedirects(followRedirects)
                        .build();

        try (Response response = requestClient.newCall(builder.build()).execute()) {
            Map<String, List<String>> responseHeaders = response.headers().toMultimap();
            if (cookieManager != null) {
                updateCookies(cookieManager, request.getURL(), responseHeaders);
            }
            ResponseBody body = response.body();
            byte[] content = body == null ? new byte[0] : body.bytes();
            return new HttpResponse(
                    response.request().url().url(), response.code(), content, responseHeaders);
        }
    }

    private static void addHeaders(Request.Builder builder,
                                   Map<String, List<String>> headers) {
        headers.forEach((name, values) -> {
            if (name == null || "content-length".equalsIgnoreCase(name)
                    || "transfer-encoding".equalsIgnoreCase(name)) {
                return;
            }
            for (String value : values) {
                builder.addHeader(name, value);
            }
        });
    }

    private static RequestBody requestBody(HttpRequest request) throws IOException {
        if (request instanceof HttpContentRequest contentRequest
                && contentRequest.hasContent()) {
            HttpContent content = contentRequest.getContent();
            MediaType mediaType = content.getContentType() == null ? null
                    : MediaType.parse(content.getContentType().toString());
            return RequestBody.create(content.getAsBytes(), mediaType);
        }
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")
                || method.equals("PROPPATCH") || method.equals("REPORT")) {
            return RequestBody.create(new byte[0]);
        }
        return null;
    }
}
