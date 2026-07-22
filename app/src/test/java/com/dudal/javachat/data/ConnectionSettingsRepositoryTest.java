package com.dudal.javachat.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectionSettingsRepositoryTest {
    @Test
    public void parsesStoredModesAndFallsBackSafely() {
        assertEquals(AuthMode.MICROSOFT,
                ConnectionSettingsRepository.parseAuthMode("MICROSOFT"));
        assertEquals(AuthMode.OFFLINE,
                ConnectionSettingsRepository.parseAuthMode("OFFLINE"));
        assertEquals(AuthMode.OFFLINE,
                ConnectionSettingsRepository.parseAuthMode("unknown"));
        assertEquals(AuthMode.OFFLINE,
                ConnectionSettingsRepository.parseAuthMode(null));
    }

    @Test
    public void validatesGlobalOfflineNickname() {
        assertTrue(ConnectionSettingsRepository.isValidOfflineNickname("Mobile_Player"));
        assertTrue(ConnectionSettingsRepository.isValidOfflineNickname("abc"));
        assertFalse(ConnectionSettingsRepository.isValidOfflineNickname("ab"));
        assertFalse(ConnectionSettingsRepository.isValidOfflineNickname("한글닉네임"));
        assertFalse(ConnectionSettingsRepository.isValidOfflineNickname("name-with-dash"));
    }
}
