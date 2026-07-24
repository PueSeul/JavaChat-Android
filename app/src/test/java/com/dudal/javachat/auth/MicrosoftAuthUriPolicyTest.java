package com.dudal.javachat.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class MicrosoftAuthUriPolicyTest {
    @Test
    public void allowsMicrosoftAndXboxHttpsPages() {
        assertTrue(MicrosoftAuthUriPolicy.isTrusted("https://www.microsoft.com/link"));
        assertTrue(MicrosoftAuthUriPolicy.isTrusted("https://login.live.com/oauth20_authorize.srf"));
        assertTrue(MicrosoftAuthUriPolicy.isTrusted(
                "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize"));
        assertTrue(MicrosoftAuthUriPolicy.isTrusted("https://sisu.xboxlive.com/connect/XboxLive"));
    }

    @Test
    public void rejectsLookalikeAndInsecurePages() {
        assertFalse(MicrosoftAuthUriPolicy.isTrusted("http://login.live.com/"));
        assertFalse(MicrosoftAuthUriPolicy.isTrusted("https://live.com.example.test/"));
        assertFalse(MicrosoftAuthUriPolicy.isTrusted("javascript:alert(1)"));
        assertFalse(MicrosoftAuthUriPolicy.isTrusted(null));
    }
}
