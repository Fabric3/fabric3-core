package org.fabric3.spi.discovery;

/**
 * A service entry.
 */
public class ServiceEntry extends AbstractEntry {
    private String path;

    public ServiceEntry() {
    }

    public ServiceEntry(String name, String address, int port, String transport) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.transport = transport;
    }

    /**
     * Returns the service path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the service path.
     *
     * @param path the path
     */
    public void setPath(String path) {
        check();
        this.path = path;
    }
}
