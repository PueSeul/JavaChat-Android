package com.dudal.javachat.protocol;

import com.dudal.javachat.auth.ConnectionIdentity;
import com.dudal.javachat.data.SavedServer;

public interface ProtocolAdapter {
    ProtocolSpec spec();
    ProtocolConnection create(SavedServer server, ConnectionIdentity identity,
                              ConnectionListener listener);
}
