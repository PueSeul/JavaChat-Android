package com.dudal.javachat.protocol;

import net.raphimc.minecraftauth.java.model.MinecraftPlayerCertificates;

import org.geysermc.mcprotocollib.protocol.data.game.ArgumentSignature;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatSessionUpdatePacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

final class SignedChatState {
    private static final int CAPACITY = 20;

    private final UUID profileId;
    private final UUID sessionId = UUID.randomUUID();
    private final MinecraftPlayerCertificates certificates;
    private final SecureRandom random = new SecureRandom();
    private final byte[][] lastSeen = new byte[CAPACITY][];

    private int sessionIndex;
    private int nextSlot;
    private int pending;

    SignedChatState(UUID profileId, MinecraftPlayerCertificates certificates) {
        this.profileId = profileId;
        this.certificates = certificates;
    }

    ServerboundChatSessionUpdatePacket sessionUpdatePacket() {
        // Java 26.2's ProfilePublicKey.Data codec expects publicKeySignatureV2.
        // MinecraftAuth exposes that field as getPublicKeySignature(); the
        // similarly named legacy value is for the pre-v2 signed payload.
        byte[] keySignature = certificates.getPublicKeySignature();
        if (keySignature == null || keySignature.length == 0) {
            throw new IllegalStateException(
                    "Java 26.2용 프로필 공개키 서명이 인증서에 없습니다. Microsoft 로그인을 다시 진행해 주세요.");
        }
        return new ServerboundChatSessionUpdatePacket(
                sessionId,
                certificates.getExpireTimeMs(),
                certificates.getKeyPair().getPublic(),
                keySignature);
    }

    synchronized void observe(byte[] signature) {
        if (signature == null || signature.length == 0) {
            return;
        }
        lastSeen[nextSlot] = signature.clone();
        nextSlot = (nextSlot + 1) % CAPACITY;
        pending++;
    }

    synchronized int takePendingAckCountIfRequired() {
        if (pending <= 64) {
            return 0;
        }
        int value = pending;
        pending = 0;
        return value;
    }

    synchronized ServerboundChatPacket createPacket(String message) throws Exception {
        long timestamp = System.currentTimeMillis();
        long salt = random.nextLong();
        Acknowledgements ack = acknowledgements();
        byte[] signature = sign(message, timestamp, salt, ack.signatures);
        int offset = pending;
        pending = 0;
        return new ServerboundChatPacket(
                message,
                timestamp,
                salt,
                signature,
                offset,
                ack.bitSet,
                checksum());
    }

    synchronized ServerboundChatCommandSignedPacket createCommandPacket(
            String command, List<CommandArgument> arguments) throws Exception {
        long timestamp = System.currentTimeMillis();
        long salt = random.nextLong();
        Acknowledgements ack = acknowledgements();
        List<ArgumentSignature> signatures = new ArrayList<>();
        for (CommandArgument argument : arguments) {
            if (signatures.size() == 8) {
                break;
            }
            signatures.add(new ArgumentSignature(
                    argument.getName(),
                    sign(argument.getValue(), timestamp, salt, ack.signatures)));
        }
        int offset = pending;
        pending = 0;
        return new ServerboundChatCommandSignedPacket(
                command,
                timestamp,
                salt,
                signatures,
                offset,
                ack.bitSet,
                (byte) checksum());
    }

    private Acknowledgements acknowledgements() {
        List<byte[]> signatures = new ArrayList<>();
        BitSet bitSet = new BitSet(CAPACITY);
        for (int index = 0; index < CAPACITY; index++) {
            int slot = (nextSlot + index) % CAPACITY;
            byte[] signature = lastSeen[slot];
            if (signature != null) {
                bitSet.set(index);
                signatures.add(signature);
            }
        }
        return new Acknowledgements(bitSet, signatures);
    }

    private byte[] sign(String message, long timestamp, long salt, List<byte[]> acknowledgements)
            throws Exception {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(1);
            writeUuid(out, profileId);
            writeUuid(out, sessionId);
            out.writeInt(sessionIndex++);
            out.writeLong(salt);
            out.writeLong(timestamp / 1000L);
            out.writeInt(messageBytes.length);
            out.write(messageBytes);
            out.writeInt(acknowledgements.size());
            for (byte[] acknowledged : acknowledgements) {
                out.write(acknowledged);
            }
        }

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(certificates.getKeyPair().getPrivate());
        signer.update(bytes.toByteArray());
        return signer.sign();
    }

    private int checksum() {
        int checksum = 1;
        for (byte[] signature : lastSeen) {
            if (signature != null) {
                checksum = 31 * checksum + Arrays.hashCode(signature);
            }
        }
        int result = checksum & 0xff;
        return result == 0 ? 1 : result;
    }

    private static void writeUuid(DataOutputStream out, UUID id) throws Exception {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
    }

    private static final class Acknowledgements {
        private final BitSet bitSet;
        private final List<byte[]> signatures;

        private Acknowledgements(BitSet bitSet, List<byte[]> signatures) {
            this.bitSet = bitSet;
            this.signatures = signatures;
        }
    }
}
