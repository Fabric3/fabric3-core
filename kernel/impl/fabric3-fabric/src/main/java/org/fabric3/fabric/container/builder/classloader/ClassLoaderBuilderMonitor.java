package org.fabric3.fabric.container.builder.classloader;

import org.fabric3.api.annotation.monitor.Info;

/**
 *
 */
public interface ClassLoaderBuilderMonitor {

    @Info("Dynamic native libraries not supported on this JVM")
    void nativeLibrariesNotSupported();

}
