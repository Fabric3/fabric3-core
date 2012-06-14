package org.fabric3.contribution;

import org.fabric3.api.annotation.monitor.Info;

/**
 * @version $Rev$ $Date$
 */
public interface ContributionLoaderMonitor {

    @Info("Dynamic native libraries not supported on this JVM")
    void nativeLibrariesNotSupported();
}
