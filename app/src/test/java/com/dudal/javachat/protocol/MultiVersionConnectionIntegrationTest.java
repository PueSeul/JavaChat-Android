package com.dudal.javachat.protocol;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.platform.NoopInjector;

import net.kyori.adventure.text.Component;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.netty.DefaultPacketHandlerExecutor;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.HandPreference;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ChatVisibility;
import org.geysermc.mcprotocollib.protocol.data.game.setting.ParticleStatus;
import org.geysermc.mcprotocollib.protocol.data.game.setting.SkinPart;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundPingPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundClientInformationPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundPongPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDisguisedChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandSuggestionsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundCommandSuggestionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginFinishedPacket;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiVersionConnectionIntegrationTest {
    private static final String USERNAME = "CodexTest";

    @BeforeClass
    public static void loadViaVersion() {
        if (!Via.isLoaded()) {
            File folder = new File("build/test-viaversion-integration");
            assertTrue(folder.exists() || folder.mkdirs());
            ViaManagerImpl.initAndLoad(
                    new ViaProtocolCoverageTest.TestPlatform(folder),
                    new NoopInjector(),
                    new ViaCommandHandler(false),
                    new ViaTranslationRuntime.AndroidPlatformLoader());
        } else {
            new ViaTranslationRuntime.AndroidPlatformLoader().load();
        }
    }

    @Test
    public void connectsAndChatsAcrossRepresentativeReleaseGenerations() throws Exception {
        List<Case> cases = List.of(
                new Case("java-1.8.9", 25581),
                new Case("java-1.12.2", 25582),
                new Case("java-1.16.5", 25583),
                new Case("java-1.19.4", 25584),
                new Case("java-1.21.11", 25585));
        Assume.assumeTrue("Local integration servers are not running",
                cases.stream().allMatch(value -> isListening(value.port)));

        for (Case value : cases) {
            connectAndChat(value);
        }
    }

    private static void connectAndChat(Case value) throws Exception {
        ProtocolSpec target = ProtocolRegistry.require(value.versionId);
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + USERNAME)
                .getBytes(StandardCharsets.UTF_8));
        MinecraftProtocol protocol = new MinecraftProtocol(
                new GameProfile(offlineId, USERNAME), null);
        ClientNetworkSession session = new TranslatedClientNetworkSession(
                InetSocketAddress.createUnresolved("127.0.0.1", value.port),
                protocol,
                DefaultPacketHandlerExecutor.createExecutor(),
                null,
                null,
                target);
        session.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, new SessionService());

        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch commandReply = new CountDownLatch(1);
        CountDownLatch playerList = new CountDownLatch(1);
        CountDownLatch suggestions = new CountDownLatch(1);
        AtomicReference<String> disconnect = new AtomicReference<>();
        List<String> received = new CopyOnWriteArrayList<>();
        PlayerTracker players = new PlayerTracker();
        String message = "hello-" + target.getDisplayName().substring(5);
        session.addListener(new SessionAdapter() {
            @Override
            public void packetReceived(Session active, Packet packet) {
                String text = chatText(packet);
                received.add(packet.getClass().getSimpleName()
                        + (text.isBlank() ? "" : "=" + text));
                if (packet instanceof ClientboundLoginFinishedPacket) {
                    active.send(clientInformation());
                } else if (packet instanceof ClientboundLoginPacket) {
                    // Client information is legal in both configuration and game.
                    // Repeating it here is important for very old servers after
                    // ViaVersion has synthesized the configuration phase.
                    active.send(clientInformation());
                } else if (packet instanceof ClientboundPlayerPositionPacket position) {
                    active.send(new ServerboundAcceptTeleportationPacket(position.getId()));
                    ready.countDown();
                } else if (packet instanceof ClientboundPingPacket ping) {
                    active.send(new ServerboundPongPacket(ping.getId()));
                }
                if (packet instanceof org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket update
                        && players.apply(update).stream()
                        .anyMatch(player -> USERNAME.equals(player.getProfileName()))) {
                    playerList.countDown();
                }
                if (!text.isBlank()) {
                    commandReply.countDown();
                }
                if (packet instanceof ClientboundCommandSuggestionsPacket reply
                        && reply.getTransactionId() == 42
                        && reply.getMatches().length > 0) {
                    suggestions.countDown();
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                disconnect.set(ComponentText.plain(event.getReason()));
                ready.countDown();
                commandReply.countDown();
                playerList.countDown();
                suggestions.countDown();
            }
        });

        try {
            session.connect(true);
            assertTrue(target.getDisplayName() + " login failed: " + disconnect.get(),
                    ready.await(35, TimeUnit.SECONDS) && session.isConnected()
                            && disconnect.get() == null);
            assertTrue(target.getDisplayName() + " player list failed: " + received,
                    playerList.await(10, TimeUnit.SECONDS) && disconnect.get() == null);
            session.send(new ServerboundChatPacket(
                    message,
                    Instant.now().toEpochMilli(),
                    0L,
                    null,
                    0,
                    new BitSet(20),
                    1));
            session.send(new ServerboundChatCommandPacket("list"));
            session.send(new ServerboundCommandSuggestionPacket(42, "/he"));
            assertTrue(target.getDisplayName() + " chat failed: " + disconnect.get()
                            + " autoRead=" + session.getChannel().config().isAutoRead()
                            + " pipeline=" + session.getChannel().pipeline().names()
                            + " received=" + received,
                    commandReply.await(20, TimeUnit.SECONDS) && disconnect.get() == null);
            assertTrue(target.getDisplayName() + " command suggestions failed: " + received,
                    suggestions.await(10, TimeUnit.SECONDS) && disconnect.get() == null);
        } finally {
            if (session.isConnected()) {
                session.disconnect(Component.text("integration test complete"));
            }
        }
    }

    private static ServerboundClientInformationPacket clientInformation() {
        return new ServerboundClientInformationPacket(
                "ko_kr",
                2,
                ChatVisibility.FULL,
                true,
                Arrays.asList(SkinPart.values()),
                HandPreference.RIGHT_HAND,
                false,
                true,
                ParticleStatus.MINIMAL);
    }

    private static String chatText(Packet packet) {
        if (packet instanceof ClientboundSystemChatPacket chat && !chat.isOverlay()) {
            return ComponentText.plain(chat.getContent());
        }
        if (packet instanceof ClientboundDisguisedChatPacket chat) {
            return ComponentText.plain(chat.getMessage());
        }
        if (packet instanceof ClientboundPlayerChatPacket chat) {
            return chat.getUnsignedContent() == null
                    ? chat.getContent() : ComponentText.plain(chat.getUnsignedContent());
        }
        return "";
    }

    private static boolean isListening(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private record Case(String versionId, int port) {
    }
}
