package org.fabric3.spi.discovery;

/**
 * Base discovery entry.
 */
public abstract class AbstractEntry {
    private String key;
    private String binding;

    private String address;
    private int port;

    /**
     * Returns the discovery entry key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the discovery entry key.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the binding type.
     *
     * @return the binding type
     */
    public String getBinding() {
        return binding;
    }

    /**
     * Sets the binding type.
     *
     * @param binding the binding type
     */
    public void setBinding(String binding) {
        this.binding = binding;
    }

    /**
     * Returns the host address.
     *
     * @return the host address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the host address
     *
     * @param address the host address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the port number.
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number.
     *
     * @param port the port number
     */
    public void setPort(int port) {
        this.port = port;
    }
}
