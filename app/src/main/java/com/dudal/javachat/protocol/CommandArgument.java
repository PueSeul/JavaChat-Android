package com.dudal.javachat.protocol;

final class CommandArgument {
    private final String name;
    private final String value;

    CommandArgument(String name, String value) {
        this.name = name;
        this.value = value;
    }

    String getName() {
        return name;
    }

    String getValue() {
        return value;
    }
}
