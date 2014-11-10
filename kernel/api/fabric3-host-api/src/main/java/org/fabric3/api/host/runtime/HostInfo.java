/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.runtime;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.os.OperatingSystem;

/**
 * Interface that provides information on the host environment. This allows the runtime to access information about the environment in which it is running. The
 * implementation of this interface is provided to the runtime by the host during initialization. Hosts will generally extend this interface to provide
 * additional information.
 */
public interface HostInfo {

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

    /**
     * Returns true if the host environment is a Java EE container with XA-enabled. May be used by extensions that need to be aware they are running in an
     * XA-enabled container when creating resources (e.g. JMS transacted sessions).
     *
     * @return true if the host environment is a Java EE container with XA-enabled
     */
    boolean isJavaEEXAEnabled();
}
