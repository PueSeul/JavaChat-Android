package com.dudal.javachat.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ServerRepository {
    private static final String PREFS = "saved_servers";
    private static final String KEY_SERVERS = "servers_json";
    private static final Type LIST_TYPE = new TypeToken<ArrayList<SavedServer>>() {}.getType();

    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public ServerRepository(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized List<SavedServer> getAll() {
        String json = preferences.getString(KEY_SERVERS, "[]");
        List<SavedServer> servers = gson.fromJson(json, LIST_TYPE);
        if (servers == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(servers);
    }

    public synchronized SavedServer getById(String id) {
        for (SavedServer server : getAll()) {
            if (server.getId().equals(id)) {
                return server;
            }
        }
        return null;
    }

    public synchronized void save(SavedServer value) {
        List<SavedServer> servers = getAll();
        int index = servers.indexOf(value);
        if (index >= 0) {
            servers.set(index, value);
        } else {
            servers.add(value);
        }
        persist(servers);
    }

    public synchronized void delete(String id) {
        List<SavedServer> servers = getAll();
        servers.removeIf(server -> server.getId().equals(id));
        persist(servers);
    }

    private void persist(List<SavedServer> servers) {
        preferences.edit().putString(KEY_SERVERS, gson.toJson(servers, LIST_TYPE)).apply();
    }
}
