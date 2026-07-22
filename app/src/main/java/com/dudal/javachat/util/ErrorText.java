package com.dudal.javachat.util;

import org.geysermc.mcprotocollib.protocol.data.UnexpectedEncryptionException;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;

public final class ErrorText {
    private static final int MAX_LENGTH = 180;

    private ErrorText() {}

    public static String from(Throwable error) {
        Throwable current = root(error);
        if (current instanceof UnexpectedEncryptionException) {
            return "정품 인증 서버입니다. 온라인 방식으로 전환하고 Microsoft 로그인 후 접속해 주세요.";
        }
        if (current instanceof UnknownHostException
                || current instanceof UnresolvedAddressException) {
            return "서버 주소를 찾을 수 없습니다.";
        }
        if (current instanceof ConnectException || current instanceof NoRouteToHostException) {
            return "서버가 닫혀 있거나 포트가 올바르지 않습니다.";
        }
        if (current instanceof SocketTimeoutException) {
            return "서버 응답 시간이 초과되었습니다.";
        }
        if (current instanceof ClassNotFoundException
                && String.valueOf(current.getMessage()).contains("DnsContextFactory")) {
            return "Android DNS 호환 오류가 발생했습니다.";
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            message = current.getClass().getSimpleName();
        }
        message = message.replaceAll("\\s+", " ").trim();
        return message.length() <= MAX_LENGTH
                ? message : message.substring(0, MAX_LENGTH - 1) + "…";
    }

    private static Throwable root(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
