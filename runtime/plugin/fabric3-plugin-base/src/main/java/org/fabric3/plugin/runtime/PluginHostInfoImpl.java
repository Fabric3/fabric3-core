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
 */
package org.fabric3.plugin.runtime;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Names;
import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.api.model.type.RuntimeMode;
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
}
