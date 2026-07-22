package com.dudal.javachat.auth;

import org.junit.Test;

import java.net.UnknownHostException;

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
    public void mapsUnsupportedCertificateRequestToFriendlyMessage() {
        RuntimeException error = new RuntimeException(
                "status: 415 Unsupported Media Type, Content Type not allowed");

        assertEquals(
                "Minecraft 로그인 인증서를 발급하지 못했습니다. 잠시 후 다시 시도해 주세요.",
                AuthErrorText.from(error));
    }
}
