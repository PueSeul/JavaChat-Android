package com.dudal.javachat.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.helper.NettyHelper;
import org.geysermc.mcprotocollib.network.netty.MinecraftChannelInitializer;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

/** A normal MCProtocolLib session with ViaVersion inserted before packet decoding. */
final class TranslatedClientNetworkSession extends ClientNetworkSession {
    private final ProtocolSpec target;

    TranslatedClientNetworkSession(
            SocketAddress remoteAddress,
            MinecraftProtocol protocol,
            Executor packetHandlerExecutor,
            SocketAddress bindAddress,
            ProxyInfo proxy,
            ProtocolSpec target) {
        super(remoteAddress, protocol, packetHandlerExecutor, bindAddress, proxy);
        this.target = target;
    }

    @Override
    protected ChannelHandler getChannelHandler() {
        return new MinecraftChannelInitializer<>(channel -> {
            MinecraftProtocol protocol = getPacketProtocol();
            protocol.newClientSession(TranslatedClientNetworkSession.this);
            return TranslatedClientNetworkSession.this;
        }, true) {
            @Override
            public void initChannel(Channel channel) throws Exception {
                NettyHelper.addProxy(proxy, channel.pipeline());
                NettyHelper.initializeHAProxySupport(
                        TranslatedClientNetworkSession.this, channel);
                super.initChannel(channel);
                ViaTranslationRuntime.inject(channel, target);
            }
        };
    }
}
