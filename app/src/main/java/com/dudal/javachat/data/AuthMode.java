package com.dudal.javachat.data;

public enum AuthMode {
    MICROSOFT("온라인"),
    OFFLINE("오프라인");

    private final String label;

    AuthMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
