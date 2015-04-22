package org.fabric3.spi.discovery;

/**
 * A channel entry.
 */
public class ChannelEntry extends AbstractEntry {

    public ChannelEntry() {
    }

    public ChannelEntry(String name, String address, int port, String transport) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.transport = transport;
    }

}
