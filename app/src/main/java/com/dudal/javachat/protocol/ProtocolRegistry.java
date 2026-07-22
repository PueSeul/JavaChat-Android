package com.dudal.javachat.protocol;

import java.util.List;

/**
 * Minecraft Java release versions supported by the bundled translation engine.
 * Versions that share a wire protocol intentionally keep separate labels so a
 * saved server can match the exact version its owner advertises.
 */
public final class ProtocolRegistry {
    public static final ProtocolSpec JAVA_26_2 = spec("26.2", 776);

    private static final List<ProtocolSpec> SUPPORTED = List.of(
            JAVA_26_2,
            spec("26.1.2", 775),
            spec("26.1.1", 775),
            spec("26.1", 775),
            spec("1.21.11", 774),
            spec("1.21.10", 773),
            spec("1.21.9", 773),
            spec("1.21.8", 772),
            spec("1.21.7", 772),
            spec("1.21.6", 771),
            spec("1.21.5", 770),
            spec("1.21.4", 769),
            spec("1.21.3", 768),
            spec("1.21.2", 768),
            spec("1.21.1", 767),
            spec("1.21", 767),
            spec("1.20.6", 766),
            spec("1.20.5", 766),
            spec("1.20.4", 765),
            spec("1.20.3", 765),
            spec("1.20.2", 764),
            spec("1.20.1", 763),
            spec("1.20", 763),
            spec("1.19.4", 762),
            spec("1.19.3", 761),
            spec("1.19.2", 760),
            spec("1.19.1", 760),
            spec("1.19", 759),
            spec("1.18.2", 758),
            spec("1.18.1", 757),
            spec("1.18", 757),
            spec("1.17.1", 756),
            spec("1.17", 755),
            spec("1.16.5", 754),
            spec("1.16.4", 754),
            spec("1.16.3", 753),
            spec("1.16.2", 751),
            spec("1.16.1", 736),
            spec("1.16", 735),
            spec("1.15.2", 578),
            spec("1.15.1", 575),
            spec("1.15", 573),
            spec("1.14.4", 498),
            spec("1.14.3", 490),
            spec("1.14.2", 485),
            spec("1.14.1", 480),
            spec("1.14", 477),
            spec("1.13.2", 404),
            spec("1.13.1", 401),
            spec("1.13", 393),
            spec("1.12.2", 340),
            spec("1.12.1", 338),
            spec("1.12", 335),
            spec("1.11.2", 316),
            spec("1.11.1", 316),
            spec("1.11", 315),
            spec("1.10.2", 210),
            spec("1.10.1", 210),
            spec("1.10", 210),
            spec("1.9.4", 110),
            spec("1.9.3", 110),
            spec("1.9.2", 109),
            spec("1.9.1", 108),
            spec("1.9", 107),
            spec("1.8.9", 47)
    );

    private ProtocolRegistry() {}

    private static ProtocolSpec spec(String version, int protocolNumber) {
        return new ProtocolSpec("java-" + version, "Java " + version, protocolNumber);
    }

    public static List<ProtocolSpec> supportedVersions() {
        return SUPPORTED;
    }

    public static ProtocolSpec require(String id) {
        return SUPPORTED.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 버전입니다: " + id));
    }

    public static ProtocolAdapter adapterFor(String id) {
        return new Mc26_2ProtocolAdapter(require(id));
    }
}
