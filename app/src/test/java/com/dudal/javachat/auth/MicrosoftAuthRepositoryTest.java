package com.dudal.javachat.auth;

import net.raphimc.minecraftauth.java.model.MinecraftToken;
import net.raphimc.minecraftauth.java.request.MinecraftPlayerCertificatesRequest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MicrosoftAuthRepositoryTest {
    @Test
    public void certificateRequestUsesRequiredJsonContentType() throws Exception {
        MinecraftToken token = new MinecraftToken(
                System.currentTimeMillis() + 60_000L,
                "Bearer",
                "test-token");

        MinecraftPlayerCertificatesRequest request =
                MicrosoftAuthRepository.certificateRequest(token);

        assertTrue(request.hasContent());
        assertEquals("application/json", request.getContent().getContentType().getMimeType());
        assertEquals("{}", request.getContent().getAsString());
    }
}
