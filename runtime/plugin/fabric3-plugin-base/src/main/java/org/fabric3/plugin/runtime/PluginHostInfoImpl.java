/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.plugin.runtime;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.os.OperatingSystem;
import org.fabric3.plugin.api.runtime.PluginHostInfo;

/**
 *
 */
public class PluginHostInfoImpl implements PluginHostInfo {
    private URI domain;
    private String environment;
    private Set<URL> dependencyUrls;
    private File tempDir;
    private File buildDir;
    private File classesDir;
    private File resourcesDir;
    private File testClassesDir;
    private File testResourcesDir;
    private File baseDir;
    private OperatingSystem operatingSystem;
    private File nativeDirectory;

    public PluginHostInfoImpl(URI domain,
                              String environment,
                              Set<URL> dependencyUrls,
                              File baseDir,
                              File tempDir,
                              File buildDir,
                              File classesDir,
                              File resourcesDir,
                              File testClassesDir,
                              File testResourcesDir,
                              OperatingSystem os) {
        this.domain = domain;
        this.environment = environment;
        this.dependencyUrls = dependencyUrls;
        this.baseDir = baseDir;
        this.tempDir = tempDir;
        this.buildDir = buildDir;
        this.classesDir = classesDir;
        this.resourcesDir = resourcesDir;
        this.testClassesDir = testClassesDir;
        this.testResourcesDir = testResourcesDir;
        this.nativeDirectory = new File(tempDir, "native");
        this.operatingSystem = os;
    }

    public String getRuntimeName() {
        return "plugin";
    }

    public String getZoneName() {
        return Names.LOCAL_ZONE;
    }

    public String getEnvironment() {
        return environment;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getTempDir() {
        return tempDir;
    }

    public File getClassesDir() {
        return classesDir;
    }

    public File getResourcesDir() {
        return resourcesDir;
    }

    public File getTestClassesDir() {
        return testClassesDir;
    }

    public File getTestResourcesDir() {
        return testResourcesDir;
    }

    public File getNativeLibraryDir() {
        return nativeDirectory;
    }

    public File getDataDir() {
        // use the temp directory as the data dir
        return tempDir;
    }

    public File getUserRepositoryDirectory() {
        return null;
    }

    public File getRuntimeRepositoryDirectory() {
        return null;
    }

    public File getExtensionsRepositoryDirectory() {
        return null;
    }

    public List<File> getDeployDirectories() {
        return Collections.emptyList();
    }

    public boolean supportsClassLoaderIsolation() {
        return true;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.VM;
    }

    public URI getDomain() {
        return domain;
    }

    public Set<URL> getDependencyUrls() {
        return dependencyUrls;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public boolean isJavaEEXAEnabled() {
        return false;
    }

    public File getConfigDirectory() {
        return null;
    }
}
