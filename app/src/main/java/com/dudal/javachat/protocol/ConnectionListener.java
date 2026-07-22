package com.dudal.javachat.protocol;

import java.util.List;

public interface ConnectionListener {
    void onStateChanged(ConnectionState state, String detail);
    void onChat(ChatLine line);
    void onPlayersChanged(List<PlayerView> players);
    void onCommandSuggestions(CommandSuggestions suggestions);
}
