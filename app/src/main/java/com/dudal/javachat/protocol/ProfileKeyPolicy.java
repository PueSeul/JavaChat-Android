package com.dudal.javachat.protocol;

import java.util.UUID;

final class ProfileKeyPolicy {
    private ProfileKeyPolicy() {
    }

    /** Mirrors the official client guard: certificates belong to one online UUID. */
    static boolean canUseCertificates(UUID authenticatedProfileId, UUID serverProfileId) {
        return authenticatedProfileId != null
                && authenticatedProfileId.equals(serverProfileId);
    }
}
