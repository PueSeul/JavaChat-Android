package com.dudal.javachat.auth;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import okhttp3.Dns;

final class AuthDns implements Dns {
    interface AddressResolver {
        List<InetAddress> lookup(String hostname) throws UnknownHostException;
    }

    private final Dns systemDns;
    private final AddressResolver compatibleDns;
    private final Consumer<String> fallbackListener;

    AuthDns(Consumer<String> fallbackListener) {
        this(Dns.SYSTEM, new PublicDnsResolver(), fallbackListener);
    }

    AuthDns(Dns systemDns, AddressResolver compatibleDns,
            Consumer<String> fallbackListener) {
        this.systemDns = Objects.requireNonNull(systemDns);
        this.compatibleDns = Objects.requireNonNull(compatibleDns);
        this.fallbackListener = Objects.requireNonNull(fallbackListener);
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            return systemDns.lookup(hostname);
        } catch (UnknownHostException systemFailure) {
            if (!AuthEndpoint.supportsCompatibleDns(hostname)) {
                throw systemFailure;
            }
            try {
                fallbackListener.accept(AuthEndpoint.normalize(hostname));
                return compatibleDns.lookup(hostname);
            } catch (UnknownHostException fallbackFailure) {
                systemFailure.addSuppressed(fallbackFailure);
                throw systemFailure;
            }
        }
    }

    private static final class PublicDnsResolver implements AddressResolver {
        private static final List<String> SERVERS = List.of("1.1.1.1", "8.8.8.8");
        private static final Duration TIMEOUT = Duration.ofSeconds(2);

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            UnknownHostException failure = new UnknownHostException(
                    "Compatible DNS could not resolve " + AuthEndpoint.normalize(hostname));
            for (String server : SERVERS) {
                try {
                    SimpleResolver resolver = new SimpleResolver(server);
                    resolver.setTimeout(TIMEOUT);
                    Lookup lookup = new Lookup(hostname, Type.A);
                    lookup.setResolver(resolver);
                    lookup.setCache(new Cache(DClass.IN));
                    Record[] records = lookup.run();
                    List<InetAddress> addresses = new ArrayList<>();
                    if (records != null) {
                        for (Record record : records) {
                            if (record instanceof ARecord addressRecord) {
                                addresses.add(addressRecord.getAddress());
                            }
                        }
                    }
                    if (!addresses.isEmpty()) {
                        return List.copyOf(addresses);
                    }
                    failure.addSuppressed(new UnknownHostException(
                            server + ": " + lookup.getErrorString()));
                } catch (Exception error) {
                    failure.addSuppressed(error);
                }
            }
            throw failure;
        }
    }
}
