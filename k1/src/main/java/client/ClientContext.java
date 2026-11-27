package client;

import protocol.NetworkClient;

/** Edinyy NetworkClient na vsyu client-skuyu sesiyu. */
public final class ClientContext {
    private static final NetworkClient CLIENT = new NetworkClient();

    private ClientContext() {}

    public static NetworkClient client() {
        return CLIENT;
    }
}
