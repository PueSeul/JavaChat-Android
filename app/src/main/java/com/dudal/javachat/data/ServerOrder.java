package com.dudal.javachat.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Applies a user-selected order while preserving servers not present in the request. */
public final class ServerOrder {
    private ServerOrder() {}

    public static List<SavedServer> apply(List<SavedServer> current, List<String> orderedIds) {
        Map<String, SavedServer> remaining = new LinkedHashMap<>();
        for (SavedServer server : current) {
            remaining.put(server.getId(), server);
        }

        List<SavedServer> result = new ArrayList<>(current.size());
        for (String id : orderedIds) {
            SavedServer server = remaining.remove(id);
            if (server != null) {
                result.add(server);
            }
        }
        result.addAll(remaining.values());
        return result;
    }
}
