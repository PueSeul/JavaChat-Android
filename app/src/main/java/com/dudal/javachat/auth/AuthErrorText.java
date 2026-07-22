package com.dudal.javachat.auth;

import com.dudal.javachat.util.ErrorText;

import net.raphimc.minecraftauth.java.exception.MinecraftProfileNotFoundException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public final class AuthErrorText {
    private AuthErrorText() {
    }

    public static String from(Throwable error) {
        boolean dnsFailure = false;
        boolean timeout = false;
        AuthDnsException authDnsFailure = null;
        AuthConnectionException authConnectionFailure = null;
        StringBuilder messages = new StringBuilder();
        Throwable current = error;
        while (current != null) {
            if (current instanceof MinecraftProfileNotFoundException) {
                return noMinecraftProfile();
            }
            if (current instanceof UnknownHostException) {
                dnsFailure = true;
            }
            if (current instanceof AuthDnsException detailedDnsFailure) {
                authDnsFailure = detailedDnsFailure;
            }
            if (current instanceof AuthConnectionException detailedConnectionFailure) {
                authConnectionFailure = detailedConnectionFailure;
            }
            if (current instanceof SocketTimeoutException) {
                timeout = true;
            }
            String message = current.getMessage();
            if (message != null) {
                messages.append(' ').append(message.toLowerCase(java.util.Locale.ROOT));
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }

        String combined = messages.toString();
        if (combined.contains("doesn't have a minecraft profile")
                || combined.contains("minecraft profile not found")) {
            return noMinecraftProfile();
        }
        if (authDnsFailure != null) {
            String host = authDnsFailure.getHost();
            return AuthEndpoint.label(host) + " 서버(" + host + ")의 주소를 찾지 못했습니다. "
                    + authDnsFailure.getRetryCount() + "회 재시도했지만 연결되지 않았습니다.";
        }
        if (authConnectionFailure != null) {
            String host = authConnectionFailure.getHost();
            return AuthEndpoint.label(host) + " 서버(" + host + ") 연결이 중단됐습니다. "
                    + authConnectionFailure.getRetryCount()
                    + "회 재시도했지만 연결되지 않았습니다.";
        }
        if (dnsFailure || combined.contains("eai_nodata")
                || combined.contains("no address associated with hostname")) {
            return "Microsoft 인증 서버 주소를 찾지 못했습니다. 인터넷 연결 또는 DNS 설정을 확인한 뒤 다시 시도해 주세요.";
        }
        if (timeout) {
            return "Microsoft 인증 서버의 응답 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
        }
        if (combined.contains("415 unsupported media type")
                || (combined.contains("content type") && combined.contains("not allowed"))) {
            return "Minecraft 로그인 인증서를 발급하지 못했습니다. 잠시 후 다시 시도해 주세요.";
        }
        return ErrorText.from(error);
    }

    private static String noMinecraftProfile() {
        return "이 Microsoft 계정에서 Minecraft Java Edition 계정을 찾지 못했습니다. Java Edition 보유 여부와 Minecraft 프로필 생성을 확인해 주세요.";
    }
}
