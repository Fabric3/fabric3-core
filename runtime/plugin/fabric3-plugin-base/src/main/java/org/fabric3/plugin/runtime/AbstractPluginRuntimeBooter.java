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
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.monitor.DestinationRouter;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.stream.InputStreamSource;
import org.fabric3.host.stream.Source;
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

    public PluginRuntime boot() throws InitializationException {
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

        coordinator = bootstrapService.createCoordinator(configuration);
        coordinator.start();

        return runtime;
    }

    public void shutdown() throws ShutdownException, InterruptedException, ExecutionException {
        coordinator.shutdown();
    }

    protected abstract String getPluginClass();

    protected abstract Map<String, String> getExportedPackages();

    protected abstract PluginHostInfo createHostInfo(String environment, Set<URL> moduleDependencies, File outputDirectory, File buildDir);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private PluginRuntime createRuntime(BootstrapService bootstrapService, Document systemConfig, File buildDir) throws InitializationException {
        String environment = bootstrapService.parseEnvironment(systemConfig);

        PluginHostInfo hostInfo = createHostInfo(environment, moduleDependencies, outputDirectory, buildDir);

        File tempDir = hostInfo.getTempDir();
        tempDir.mkdir();

        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer(PluginConstants.DOMAIN);

        PluginRuntimeConfiguration configuration = new PluginRuntimeConfiguration(hostInfo, mBeanServer, router, system, session);

        return instantiateRuntime(configuration, bootClassLoader);
    }

    private Document getSystemConfig(BootstrapService bootstrapService) throws InitializationException {
        Source source = null;
        if (systemConfig != null) {
            try {
                InputStream stream = new ByteArrayInputStream(systemConfig.getBytes("UTF-8"));
                source = new InputStreamSource("systemConfig", stream);
            } catch (UnsupportedEncodingException e) {
                throw new InitializationException("Error loading system configuration", e);
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
        } catch (ClassNotFoundException e) {
            // programming error
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

}
