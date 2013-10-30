package org.fabric3.api.binding.zeromq.model;

import java.io.Serializable;

/**
 * A Zero MQ socket address specified on a binding definition.
 */
public class SocketAddressDefinition implements Serializable {
    private static final long serialVersionUID = 6281835605823447232L;

    public static final int UNDEFINED = -1;

    private String host;
    private int port = UNDEFINED;

    public SocketAddressDefinition(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
