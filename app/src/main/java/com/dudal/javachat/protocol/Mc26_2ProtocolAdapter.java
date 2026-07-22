package com.dudal.javachat.protocol;

import com.dudal.javachat.auth.ConnectionIdentity;
import com.dudal.javachat.data.SavedServer;

public final class Mc26_2ProtocolAdapter implements ProtocolAdapter {
    private final ProtocolSpec spec;

    public Mc26_2ProtocolAdapter(ProtocolSpec spec) {
        this.spec = spec;
    }

    @Override
    public ProtocolSpec spec() {
        return spec;
    }

    @Override
    public ProtocolConnection create(SavedServer server, ConnectionIdentity identity,
                                     ConnectionListener listener) {
        return new Mc26_2Connection(server, identity, listener, spec);
    }
}
