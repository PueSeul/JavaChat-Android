package com.dudal.javachat.auth;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AuthDnsTest {
    @Test
    public void usesSystemDnsWhenItSucceeds() throws Exception {
        InetAddress systemAddress = InetAddress.getByAddress(
                "login.microsoftonline.com", new byte[]{1, 2, 3, 4});
        int[] fallbackCalls = {0};
        AuthDns dns = new AuthDns(
                hostname -> List.of(systemAddress),
                hostname -> {
                    fallbackCalls[0]++;
                    return List.of();
                },
                hostname -> { });

        assertEquals(List.of(systemAddress), dns.lookup("login.microsoftonline.com"));
        assertEquals(0, fallbackCalls[0]);
    }

    @Test
    public void usesCompatibleDnsOnlyForKnownAuthenticationHosts() throws Exception {
        InetAddress fallbackAddress = InetAddress.getByAddress(
                "xsts.auth.xboxlive.com", new byte[]{5, 6, 7, 8});
        List<String> fallbackEvents = new ArrayList<>();
        AuthDns dns = new AuthDns(
                failingSystemDns(),
                hostname -> List.of(fallbackAddress),
                fallbackEvents::add);

        assertEquals(List.of(fallbackAddress), dns.lookup("xsts.auth.xboxlive.com"));
        assertEquals(List.of("xsts.auth.xboxlive.com"), fallbackEvents);
        assertThrows(UnknownHostException.class,
                () -> dns.lookup("unrelated.example"));
    }

    private static Dns failingSystemDns() {
        return hostname -> {
            throw new UnknownHostException(hostname);
        };
    }
}
