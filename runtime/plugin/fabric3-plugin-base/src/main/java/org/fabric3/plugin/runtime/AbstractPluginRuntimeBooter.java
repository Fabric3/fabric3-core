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

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.BootstrapFactory;
import org.fabric3.api.host.runtime.BootstrapService;
import org.fabric3.api.host.runtime.ComponentRegistration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.stream.InputStreamSource;
import org.fabric3.api.host.stream.Source;
import org.fabric3.plugin.api.runtime.PluginHostInfo;
import org.fabric3.plugin.api.runtime.PluginRuntime;
import org.fabric3.plugin.api.runtime.PluginRuntimeConfiguration;
import org.w3c.dom.Document;

/**
 * Base plugin runtime booter.
 */
public abstract class AbstractPluginRuntimeBooter {
    private File buildDir;
    private File outputDirectory;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private RepositorySystem system;
    private RepositorySystemSession session;
    private DestinationRouter router;

    private RuntimeCoordinator coordinator;
    private List<ContributionSource> contributions;

    public AbstractPluginRuntimeBooter(PluginBootConfiguration configuration) {
        outputDirectory = configuration.getOutputDirectory();
        systemConfig = configuration.getSystemConfig();
        bootClassLoader = configuration.getBootClassLoader();
        hostClassLoader = configuration.getHostClassLoader();
        moduleDependencies = configuration.getModuleDependencies();
        contributions = configuration.getExtensions();
        system = configuration.getRepositorySystem();
        session = configuration.getRepositorySession();
        router = configuration.getRouter();
        buildDir = configuration.getBuildDir();
    }

    public PluginRuntime boot() throws Fabric3Exception {
        BootstrapService bootstrapService = BootstrapFactory.getService(bootClassLoader);
        Document systemConfig = getSystemConfig(bootstrapService);

        PluginRuntime runtime = createRuntime(bootstrapService, systemConfig, buildDir);

        Map<String, String> exportedPackages = getExportedPackages();
        exportedPackages.put("org.fabric3.test.spi", Names.VERSION);
        exportedPackages.put("org.fabric3.plugin.api", Names.VERSION);
        exportedPackages.put("org.junit", PluginConstants.JUNIT_VERSION);

        BootConfiguration configuration = new BootConfiguration();

        configuration.setRuntime(runtime);
        configuration.setHostClassLoader(hostClassLoader);
        configuration.setBootClassLoader(bootClassLoader);

        configuration.setSystemConfig(systemConfig);
        configuration.setExtensionContributions(contributions);
        configuration.setExportedPackages(exportedPackages);

        List<ComponentRegistration> registrations = bootstrapService.createDefaultRegistrations(runtime);
        configuration.addRegistrations(registrations);

        coordinator = bootstrapService.createCoordinator(configuration);
        coordinator.start();

        return runtime;
    }

    public void shutdown() throws Fabric3Exception, InterruptedException, ExecutionException {
        coordinator.shutdown();
    }

    protected abstract String getPluginClass();

    protected abstract Map<String, String> getExportedPackages();

    protected abstract PluginHostInfo createHostInfo(String environment, Set<URL> moduleDependencies, File outputDirectory, File buildDir);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PluginRuntime createRuntime(BootstrapService bootstrapService, Document systemConfig, File buildDir) throws Fabric3Exception {
        String environment = bootstrapService.parseEnvironment(systemConfig);

        PluginHostInfo hostInfo = createHostInfo(environment, moduleDependencies, outputDirectory, buildDir);

        File tempDir = hostInfo.getTempDir();
        tempDir.mkdir();

        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer(PluginConstants.DOMAIN);

        PluginRuntimeConfiguration configuration = new PluginRuntimeConfiguration(hostInfo, mBeanServer, router, system, session);

        return instantiateRuntime(configuration, bootClassLoader);
    }

    private Document getSystemConfig(BootstrapService bootstrapService) throws Fabric3Exception {
        Source source = null;
        if (systemConfig != null) {
            try {
                InputStream stream = new ByteArrayInputStream(systemConfig.getBytes("UTF-8"));
                source = new InputStreamSource("systemConfig", stream);
            } catch (UnsupportedEncodingException e) {
                throw new Fabric3Exception("Error loading system configuration", e);
            }
        }
        Document systemConfig;
        systemConfig = source == null ? bootstrapService.createDefaultSystemConfig() : bootstrapService.loadSystemConfig(source);
        return systemConfig;
    }

    @SuppressWarnings("unchecked")
    private PluginRuntime instantiateRuntime(PluginRuntimeConfiguration configuration, ClassLoader cl) {
        try {
            Class<?> implClass = cl.loadClass(getPluginClass());
            return PluginRuntime.class.cast(implClass.getConstructor(PluginRuntimeConfiguration.class).newInstance(configuration));
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            // programming error
            throw new AssertionError(e);
        }
    }

}
