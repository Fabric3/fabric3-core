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
*/
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

public class MavenBootConfiguration {
    private int mavenVersion;
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private Set<Dependency> extensions;
    private Log log;
    private ExtensionHelper extensionHelper;
    private Set<ArtifactRepository> repositories = Collections.emptySet();

    public void setMavenVersion(int mavenVersion) {
        this.mavenVersion = mavenVersion;
    }

    public int getMavenVersion() {
        return mavenVersion;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getSystemConfigDir() {
        return systemConfigDir;
    }

    public void setSystemConfigDir(String systemConfigDir) {
        this.systemConfigDir = systemConfigDir;
    }

    public String getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(String systemConfig) {
        this.systemConfig = systemConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Set<URL> getModuleDependencies() {
        return moduleDependencies;
    }

    public void setModuleDependencies(Set<URL> moduleDependencies) {
        this.moduleDependencies = moduleDependencies;
    }

    public Set<Dependency> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<Dependency> extensions) {
        this.extensions = extensions;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public ExtensionHelper getExtensionHelper() {
        return extensionHelper;
    }

    public void setExtensionHelper(ExtensionHelper extensionHelper) {
        this.extensionHelper = extensionHelper;
    }

    public Set<ArtifactRepository> getRepositories() {
        return repositories;
    }

    public void setRemoteRepositories(Set<ArtifactRepository> repositories) {
        this.repositories = repositories;
    }
}
