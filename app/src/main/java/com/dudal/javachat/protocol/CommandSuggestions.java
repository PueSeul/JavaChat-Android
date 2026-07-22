package com.dudal.javachat.protocol;

import java.util.List;

public final class CommandSuggestions {
    private final String requestText;
    private final int start;
    private final int length;
    private final List<String> matches;

    public CommandSuggestions(String requestText, int start, int length, List<String> matches) {
        this.requestText = requestText;
        this.start = start;
        this.length = length;
        this.matches = List.copyOf(matches);
    }

    public String getRequestText() {
        return requestText;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public List<String> getMatches() {
        return matches;
    }
}
