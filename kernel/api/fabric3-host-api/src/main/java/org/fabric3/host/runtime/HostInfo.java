/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host.runtime;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.os.OperatingSystem;

/**
 * Interface that provides information on the host environment. This allows the runtime to access information about the environment in which it is
 * running. The implementation of this interface is provided to the runtime by the host during initialization. Hosts will generally extend this
 * interface to provide additional information.
 */
public interface HostInfo {

    /**
     * Returns the unique name associated with this runtime. Names survive restarts.
     *
     * @return the unique runtime name
     */
    String getRuntimeName();

    /**
     * Returns the mode the runtime is booted in.
     *
     * @return the mode the runtime is booted in
     */
    RuntimeMode getRuntimeMode();

    /**
     * Returns the SCA domain associated with this runtime.
     *
     * @return the SCA domain associated with this runtime
     */
    URI getDomain();

    /**
     * Returns the runtime environment type.
     *
     * @return the runtime environment type
     */
    String getEnvironment();

    /**
     * Gets the base directory for the runtime.
     *
     * @return The base directory for the runtime or null if the runtime does not support persistent capabilities
     */
    File getBaseDir();

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

    /**
     * Returns the temporary directory where native libraries are extracted.
     *
     * @return the temporary directory where native libraries are extracted
     */
    File getNativeLibraryDir();

    /**
     * Returns the directory containing this runtime's configuration.
     *
     * @return the directory containing this runtime's configuration or null if the runtime does not support external configuration
     */
    File getConfigDirectory();

    /**
     * Returns the user repository directory.
     *
     * @return the user repository directory or null if the runtime provisions extensions from an external source
     */
    File getUserRepositoryDirectory();

    /**
     * Returns the private runtime repository directory.
     *
     * @return the private runtime repository directory or null if the runtime provisions extensions from an external source
     */
    File getRuntimeRepositoryDirectory();

    /**
     * Returns the shared runtime extensions directory.
     *
     * @return the shared runtime extensions directory
     */
    File getExtensionsRepositoryDirectory();

    /**
     * Returns the runtime deploy directories.
     *
     * @return the runtime deploy directories or null if the runtime does not support file system-based deployment
     */
    List<File> getDeployDirectories();

    /**
     * True if the host environment supports classloader isolation.
     *
     * @return true if the host environment supports classloader isolation
     */
    boolean supportsClassLoaderIsolation();

    /**
     * Returns the current operating system.
     *
     * @return the current operating system.
     */
    OperatingSystem getOperatingSystem();
}
