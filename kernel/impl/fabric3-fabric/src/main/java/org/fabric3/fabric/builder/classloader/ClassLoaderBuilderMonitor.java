package org.fabric3.fabric.builder.classloader;

import org.fabric3.api.annotation.monitor.Info;

/**
 * @version $Rev$ $Date$
 */
public interface ClassLoaderBuilderMonitor {

    @Info("Dynamic native libraries not supported on this JVM")
    void nativeLibrariesNotSupported();

}
