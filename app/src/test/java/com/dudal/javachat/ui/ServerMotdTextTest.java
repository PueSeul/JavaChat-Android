package com.dudal.javachat.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ServerMotdTextTest {
    @Test
    public void trimsServerAlignmentSpacesAndKeepsFormatting() {
        String raw = "\u00A7a                 Hypixel Network \u00A7c[1.8/26.2]\r\n"
                + "         \u00A7bSKYBLOCK \u00A7f| \u00A7eSUMMER EVENT   ";

        assertEquals(
                "\u00A7aHypixel Network \u00A7c[1.8/26.2]\n"
                        + "\u00A7bSKYBLOCK \u00A7f| \u00A7eSUMMER EVENT",
                ServerMotdText.normalize(raw));
    }
}
