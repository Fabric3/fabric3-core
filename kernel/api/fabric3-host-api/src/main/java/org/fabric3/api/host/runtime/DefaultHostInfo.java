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

import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.api.model.type.RuntimeMode;

/**
 * Default HostInfo implementation.
 */
public class DefaultHostInfo implements HostInfo {
    private String runtimeName;
    private String zoneName;
    private RuntimeMode runtimeMode;
    private String environment;
    private URI domain;
    private File baseDir;
    private File sharedDirectory;
    private File tempDirectory;
    private File nativeDirectory;

    private List<File> deployDirectories;
    private OperatingSystem operatingSystem;
    private boolean javaEEXAEnabled;
    private File dataDirectory;

    /**
     * Constructor.
     *
     * @param runtimeName       the runtime name
     * @param zoneName          the zone name
     * @param runtimeMode       the mode the runtime is started in
     * @param environment       the runtime environment type
     * @param domain            the SCA domain this runtime belongs to
     * @param baseDir           directory containing the standalone installation
     * @param sharedDirectory   the shared extensions repository directory
     * @param dataDirectory     directory for storing persistent data
     * @param tempDirectory     the directory for writing temporary files
     * @param deployDirectories the directory for file system-based deployments
     * @param operatingSystem   the current operating system
     * @param javaEEXAEnabled   true if the host is a Java EE XA-enabled container
     */
    public DefaultHostInfo(String runtimeName,
                           String zoneName,
                           RuntimeMode runtimeMode,
                           String environment,
                           URI domain,
                           File baseDir,
                           File sharedDirectory,
                           File dataDirectory,
                           File tempDirectory,
                           List<File> deployDirectories,
                           OperatingSystem operatingSystem,
                           boolean javaEEXAEnabled) {
        this.runtimeName = runtimeName;
        this.zoneName = zoneName;
        this.runtimeMode = runtimeMode;
        this.environment = environment;
        this.domain = domain;
        this.baseDir = baseDir;
        this.sharedDirectory = sharedDirectory;
        this.dataDirectory = dataDirectory;
        this.tempDirectory = tempDirectory;
        this.javaEEXAEnabled = javaEEXAEnabled;
        this.nativeDirectory = new File(tempDirectory, "native");
        this.deployDirectories = deployDirectories;
        this.operatingSystem = operatingSystem;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public String getEnvironment() {
        return environment;
    }

    public URI getDomain() {
        return domain;
    }

    public final File getBaseDir() {
        return baseDir;
    }

    public File getTempDir() {
        return tempDirectory;
    }

    public File getNativeLibraryDir() {
        return nativeDirectory;
    }

    public File getDataDir() {
        return dataDirectory;
    }

    public boolean supportsClassLoaderIsolation() {
        return true;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public File getExtensionsRepositoryDirectory() {
        return sharedDirectory;
    }

    public List<File> getDeployDirectories() {
        return deployDirectories;
    }

    public boolean isJavaEEXAEnabled() {
        return javaEEXAEnabled;
    }

}
