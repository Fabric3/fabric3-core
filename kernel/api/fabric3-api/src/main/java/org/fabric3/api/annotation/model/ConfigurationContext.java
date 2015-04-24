package org.fabric3.api.annotation.model;

import java.io.File;
import java.net.URI;

/**
 *
 */
public interface ConfigurationContext {
    /**
     * Returns the unique name associated with this runtime. Names survive restarts.
     *
     * @return the unique runtime name
     */
    String getRuntimeName();

    /**
     * Returns the name of the zone this runtime is a member of.
     *
     * @return the zone name
     */
    String getZoneName();

    /**
     * Returns the domain associated with this runtime.
     *
     * @return the domain associated with this runtime
     */
    URI getDomain();

    /**
     * Returns the runtime environment type.
     *
     * @return the runtime environment type
     */
    String getEnvironment();

    /**
     * Returns the directory where persistent data can be written.
     *
     * @return the directory where persistent data can be written or null if the runtime does not support persistent capabilities
     */
    File getDataDir();

    /**
     * Returns the temporary directory.
     *
     * @return the temporary directory.
     */
    File getTempDir();

}
