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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.contribution.ContributionSource;
import org.w3c.dom.Document;

/**
 * Encapsulates configuration needed to bootstrap a runtime.
 */
public class BootConfiguration {
    private Fabric3Runtime runtime;
    private Document systemConfig;
    private ClassLoader bootClassLoader;
    private Map<String, String> exportedPackages = new HashMap<>();
    private List<String> hostCapabilities = new ArrayList<>();
    private List<ComponentRegistration> registrations = new ArrayList<>();
    private List<ContributionSource> extensionContributions = Collections.emptyList();
    private List<ContributionSource> userContributions = Collections.emptyList();
    private ClassLoader hostClassLoader;

    public Fabric3Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Fabric3Runtime runtime) {
        this.runtime = runtime;
    }

    public Document getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(Document systemConfig) {
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

    public Map<String, String> getExportedPackages() {
        return exportedPackages;
    }

    public void setExportedPackages(Map<String, String> exportedPackages) {
        this.exportedPackages = exportedPackages;
    }

    public List<String> getHostCapabilities() {
        return hostCapabilities;
    }

    public void setHostCapabilities(List<String> hostCapabilities) {
        this.hostCapabilities = hostCapabilities;
    }

    public List<ComponentRegistration> getRegistrations() {
        return registrations;
    }

    public void addRegistrations(List<ComponentRegistration> registrations) {
        this.registrations.addAll(registrations);
    }

    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }

    public void setExtensionContributions(List<ContributionSource> extensionContributions) {
        this.extensionContributions = extensionContributions;
    }

    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }

    public void setUserContributions(List<ContributionSource> userContributions) {
        this.userContributions = userContributions;
    }

}
