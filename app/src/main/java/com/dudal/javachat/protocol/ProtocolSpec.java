package com.dudal.javachat.protocol;

public final class ProtocolSpec {
    private final String id;
    private final String displayName;
    private final int protocolNumber;

    public ProtocolSpec(String id, String displayName, int protocolNumber) {
        this.id = id;
        this.displayName = displayName;
        this.protocolNumber = protocolNumber;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getProtocolNumber() { return protocolNumber; }

    /**
     * Packet-layout version used by the translator. Minecraft 1.9.1 and
     * 1.9.2 have the same layout, while retaining distinct handshake numbers.
     */
    public int getTranslationProtocolNumber() {
        return protocolNumber == 108 ? 109 : protocolNumber;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
