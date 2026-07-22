package com.dudal.javachat.protocol;

import net.raphimc.minecraftauth.java.model.MinecraftPlayerCertificates;

import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatSessionUpdatePacket;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class SignedChatStateTest {
    @Test
    public void testSessionAndSignedMessagePacketShape() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        byte[] v2Signature = new byte[] {9, 8, 7, 6};
        byte[] certificateSignature = new byte[] {1, 2, 3, 4};
        MinecraftPlayerCertificates certificates = new MinecraftPlayerCertificates(
                2_000_000_000_000L,
                keyPair,
                v2Signature,
                certificateSignature);

        SignedChatState state = new SignedChatState(UUID.randomUUID(), certificates);
        ServerboundChatSessionUpdatePacket update = state.sessionUpdatePacket();
        assertEquals(2_000_000_000_000L, update.getExpiresAt());
        assertEquals(keyPair.getPublic(), update.getPublicKey());
        assertArrayEquals(v2Signature, update.getKeySignature());

        byte[] observed = new byte[256];
        observed[0] = 42;
        state.observe(observed);
        ServerboundChatPacket packet = state.createPacket("26.2 테스트");

        assertEquals("26.2 테스트", packet.getMessage());
        assertNotNull(packet.getSignature());
        assertEquals(256, packet.getSignature().length);
        assertEquals(1, packet.getOffset());
        assertFalse(packet.getAcknowledgedMessages().isEmpty());
        assertNotEquals(0, packet.getChecksum());

        ServerboundChatCommandSignedPacket command = state.createCommandPacket(
                "msg Steve 안녕하세요",
                List.of(new CommandArgument("message", "안녕하세요")));
        assertEquals("msg Steve 안녕하세요", command.getCommand());
        assertEquals(1, command.getSignatures().size());
        assertEquals("message", command.getSignatures().get(0).getName());
        assertEquals(256, command.getSignatures().get(0).getSignature().length);
        assertFalse(command.getAcknowledgedMessages().isEmpty());
        assertNotEquals(0, command.getChecksum());
    }
}
