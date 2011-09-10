/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

/**
 * Default HostInfo implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultHostInfo implements HostInfo {
    private String runtimeName;
    private RuntimeMode runtimeMode;
    private String environment;
    private URI domain;
    private File baseDir;
    private File userDirectory;
    private File sharedDirectory;
    private File runtimeDirectory;
    private File configDirectory;
    private File tempDirectory;
    private File nativeDirectory;

    private List<File> deployDirectories;
    private OperatingSystem operatingSystem;
    private File dataDirectory;

    /**
     * Constructor.
     *
     * @param runtimeName       the runtime name
     * @param runtimeMode       the mode the runtime is started in
     * @param environment       the runtime environment type
     * @param domain            the SCA domain this runtime belongs to
     * @param baseDir           directory containing the standalone installation
     * @param userDirectory     user repository directory
     * @param sharedDirectory   the shared extensions repository directory
     * @param runtimeDirectory  the private extensions repository directory
     * @param configDir         directory containing the standalone configuration
     * @param dataDirectory     directory for storing persistent data
     * @param tempDirectory     the directory for writing temporary files
     * @param deployDirectories the directory for file system-based deployments
     * @param operatingSystem   the current operating system
     */
    public DefaultHostInfo(String runtimeName,
                           RuntimeMode runtimeMode,
                           String environment,
                           URI domain,
                           File baseDir,
                           File userDirectory,
                           File sharedDirectory,
                           File runtimeDirectory,
                           File configDir,
                           File dataDirectory,
                           File tempDirectory,
                           List<File> deployDirectories,
                           OperatingSystem operatingSystem) {
        this.runtimeName = runtimeName;
        this.runtimeMode = runtimeMode;
        this.environment = environment;
        this.domain = domain;
        this.baseDir = baseDir;
        this.userDirectory = userDirectory;
        this.sharedDirectory = sharedDirectory;
        this.runtimeDirectory = runtimeDirectory;
        this.configDirectory = configDir;
        this.dataDirectory = dataDirectory;
        this.tempDirectory = tempDirectory;
        this.nativeDirectory = new File(tempDirectory, "native");
        this.deployDirectories = deployDirectories;
        this.operatingSystem = operatingSystem;
    }

    public String getRuntimeName() {
        return runtimeName;
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

    public File getUserRepositoryDirectory() {
        return userDirectory;
    }

    public File getRuntimeRepositoryDirectory() {
        return runtimeDirectory;
    }

    public File getExtensionsRepositoryDirectory() {
        return sharedDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public List<File> getDeployDirectories() {
        return deployDirectories;
    }

}
