package com.dudal.javachat.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public final class ServerOrderTest {
    @Test
    public void appliesKnownIdsAndKeepsUnmentionedServers() {
        SavedServer first = server("first");
        SavedServer second = server("second");
        SavedServer third = server("third");

        List<SavedServer> reordered = ServerOrder.apply(
                List.of(first, second, third),
                List.of("third", "unknown", "first", "third"));

        assertEquals(List.of(third, first, second), reordered);
    }

    private static SavedServer server(String id) {
        return new SavedServer(id, id, id + ".example", 25565, "auto");
    }
}
