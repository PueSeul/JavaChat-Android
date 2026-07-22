package com.dudal.javachat.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.UUID;

public class ProfileKeyPolicyTest {
    @Test
    public void allowsCertificateOnlyForSameAuthenticatedProfile() {
        UUID profileId = UUID.randomUUID();
        assertTrue(ProfileKeyPolicy.canUseCertificates(profileId, profileId));
        assertFalse(ProfileKeyPolicy.canUseCertificates(profileId, UUID.randomUUID()));
        assertFalse(ProfileKeyPolicy.canUseCertificates(profileId, null));
    }
}
