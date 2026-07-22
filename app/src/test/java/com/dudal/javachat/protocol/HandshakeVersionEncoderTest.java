package com.dudal.javachat.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HandshakeVersionEncoderTest {
    @Test
    public void rewritesOnlyTheProtocolFieldOfTheFirstHandshake() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new ViaTranslationRuntime.HandshakeVersionEncoder(108));
        ByteBuf input = Unpooled.buffer();
        writeVarInt(input, 0);
        writeVarInt(input, 109);
        input.writeByte(4);
        input.writeCharSequence("test", java.nio.charset.StandardCharsets.UTF_8);
        input.writeShort(25565);
        writeVarInt(input, 2);

        channel.writeOutbound(input);
        ByteBuf output = channel.readOutbound();
        assertEquals(0, readVarInt(output));
        assertEquals(108, readVarInt(output));
        assertEquals(4, output.readUnsignedByte());
        assertEquals("test", output.readCharSequence(4,
                java.nio.charset.StandardCharsets.UTF_8).toString());
        assertEquals(25565, output.readUnsignedShort());
        assertEquals(2, readVarInt(output));
        assertFalse(output.isReadable());
        output.release();
        channel.finishAndReleaseAll();
    }

    private static int readVarInt(ByteBuf input) {
        int value = 0;
        int position = 0;
        byte current;
        do {
            current = input.readByte();
            value |= (current & 0x7F) << position;
            position += 7;
        } while ((current & 0x80) != 0);
        return value;
    }

    private static void writeVarInt(ByteBuf output, int value) {
        while ((value & ~0x7F) != 0) {
            output.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        output.writeByte(value);
    }
}
