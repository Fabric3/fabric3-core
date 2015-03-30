package org.fabric3.spi.util;

/**
 * An auto closeable that does not throw checked exceptions.
 */
public interface Closeable extends AutoCloseable {

    /**
     * Closes the resource.
     */
    void close();

}
