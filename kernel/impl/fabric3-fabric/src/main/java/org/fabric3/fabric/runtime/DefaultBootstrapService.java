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
package org.fabric3.fabric.runtime;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.BootstrapService;
import org.fabric3.api.host.runtime.ComponentRegistration;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.runtime.ScanResult;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.node.Fabric;
import org.fabric3.fabric.runtime.bootstrap.RepositoryScanner;
import org.fabric3.fabric.runtime.bootstrap.SystemConfigLoader;
import org.w3c.dom.Document;

/**
 * Default BootstrapFactory implementation.
 */
public class DefaultBootstrapService implements BootstrapService {
    private RepositoryScanner scanner;
    private SystemConfigLoader systemConfigLoader;

    public DefaultBootstrapService() {
        scanner = new RepositoryScanner();
        systemConfigLoader = new SystemConfigLoader();
    }

    public Document loadSystemConfig(File configDirectory) throws ContainerException {
        return systemConfigLoader.loadSystemConfig(configDirectory);
    }

    public Document loadSystemConfig(Source source) throws ContainerException {
        return systemConfigLoader.loadSystemConfig(source);
    }

    public Document createDefaultSystemConfig() {
        return systemConfigLoader.createDefaultSystemConfig();
    }

    public URI parseDomainName(Document systemConfig) throws ContainerException {
        return systemConfigLoader.parseDomainName(systemConfig);
    }

    public String parseZoneName(Document systemConfig, RuntimeMode mode) throws ContainerException {
        return systemConfigLoader.parseZoneName(systemConfig, mode);
    }

    public RuntimeMode parseRuntimeMode(Document systemConfig) throws ContainerException {
        return systemConfigLoader.parseRuntimeMode(systemConfig);
    }

    public String parseEnvironment(Document systemConfig) {
        return systemConfigLoader.parseEnvironment(systemConfig);
    }

    public List<File> parseDeployDirectories(Document systemConfig) throws ContainerException {
        return systemConfigLoader.parseDeployDirectories(systemConfig);
    }

    public String parseProductName(Document systemConfig) throws ContainerException {
        return systemConfigLoader.parseProductName(systemConfig);
    }

    public String getRuntimeName(URI domainName, String zoneName, String runtimeId, RuntimeMode mode) {
        return RuntimeMode.NODE == mode ? domainName.getAuthority() + ":node:" + zoneName + ":" + runtimeId : "vm";
    }

    public ScanResult scanRepository(HostInfo info) throws ContainerException {
        return scanner.scan(info);
    }

    public Fabric3Runtime createDefaultRuntime(RuntimeConfiguration configuration) {
        return new DefaultRuntime(configuration);
    }

    public List<ComponentRegistration> createDefaultRegistrations(Fabric3Runtime runtime) {
        List<ComponentRegistration> registrations = new ArrayList<>();
        RuntimeFabric fabric = new RuntimeFabric(runtime);
        ComponentRegistration registration = new ComponentRegistration("Fabric", Fabric.class, fabric, false);
        registrations.add(registration);
        return registrations;
    }

    public RuntimeCoordinator createCoordinator(BootConfiguration configuration) {
        return new DefaultCoordinator(configuration);
    }

}