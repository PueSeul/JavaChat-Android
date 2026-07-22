package com.dudal.javachat.auth;

import org.junit.Test;

import java.net.UnknownHostException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class AuthErrorTextTest {
    @Test
    public void mapsMissingMinecraftProfileToFriendlyKoreanMessage() {
        RuntimeException error = new RuntimeException(
                "Your account doesn't have a Minecraft profile.");

        assertEquals(
                "이 Microsoft 계정에서 Minecraft Java Edition 계정을 찾지 못했습니다. "
                        + "Java Edition 보유 여부와 Minecraft 프로필 생성을 확인해 주세요.",
                AuthErrorText.from(error));
    }

    @Test
    public void doesNotMistakeDnsFailureForMissingAccount() {
        UnknownHostException error = new UnknownHostException(
                "android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)");

        assertEquals(
                "Microsoft 인증 서버 주소를 찾지 못했습니다. "
                        + "인터넷 연결 또는 DNS 설정을 확인한 뒤 다시 시도해 주세요.",
                AuthErrorText.from(error));
    }

    @Test
    public void identifiesTheAuthenticationStageAfterDnsRetries() {
        AuthDnsException error = new AuthDnsException(
                "xsts.auth.xboxlive.com", 3,
                new UnknownHostException("xsts.auth.xboxlive.com"));

        assertEquals(
                "Xbox 보안 토큰 인증 서버(xsts.auth.xboxlive.com)의 주소를 찾지 못했습니다. "
                        + "3회 재시도했지만 연결되지 않았습니다.",
                AuthErrorText.from(error));
    }

    @Test
    public void describesConnectionAbortAfterRetries() {
        AuthConnectionException error = new AuthConnectionException(
                "login.microsoftonline.com", 3,
                new SocketException("Software caused connection abort"));

        assertEquals(
                "Microsoft 계정 로그인 서버(login.microsoftonline.com) 연결이 중단됐습니다. "
                        + "3회 재시도했지만 연결되지 않았습니다.",
                AuthErrorText.from(error));
    }

    @Test
    public void mapsUnsupportedCertificateRequestToFriendlyMessage() {
        RuntimeException error = new RuntimeException(
                "status: 415 Unsupported Media Type, Content Type not allowed");

        assertEquals(
                "Minecraft 로그인 인증서를 발급하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                AuthErrorText.from(error));
    }
}
