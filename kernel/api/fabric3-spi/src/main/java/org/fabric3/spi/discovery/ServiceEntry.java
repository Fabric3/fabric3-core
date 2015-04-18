package org.fabric3.spi.discovery;

/**
 * A service entry.
 */
public class ServiceEntry extends AbstractEntry {
    private String path;

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
        this.path = path;
    }
}
