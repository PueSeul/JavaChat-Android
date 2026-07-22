package com.dudal.javachat.protocol;

import com.viaversion.viaversion.api.type.types.misc.TagType;
import com.viaversion.viaversion.codec.hash.HashFunction;

import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ViaAndroidCompatibilityTest {
    @Test
    public void bundledViaVersionUsesAndroidSafeNbtInput() throws Exception {
        Field field = TagType.class.getDeclaredField("USE_JAVA_DATA_IO");
        field.setAccessible(true);
        assertFalse(field.getBoolean(null));
    }

    @Test
    public void bundledCrc32cFallbackMatchesStandardVector() {
        byte[] bytes = "123456789".getBytes(StandardCharsets.US_ASCII);
        assertEquals(0xE3069283, HashFunction.crc32c().hashBytes(bytes));
    }
}
