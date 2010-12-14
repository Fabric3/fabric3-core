/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.management.MBeanServer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;

import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.monitor.MonitorEventDispatcherFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.stream.InputStreamSource;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.runtime.maven.MavenRuntime;

/**
 * @version $Rev$ $Date$
 */
public class MavenRuntimeBooter {
    private static final String SYSTEM_CONFIG_XML_FILE = "systemConfig.xml";
    private static final String DEFAULT_SYSTEM_CONFIG_DIR = "test-classes" + File.separator + "META-INF" + File.separator;
    private static final String RUNTIME_IMPL = "org.fabric3.runtime.maven.impl.MavenRuntimeImpl";
    private static final String DOMAIN = "fabric3://domain";

    // configuration elements
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private Set<org.apache.maven.model.Dependency> extensions;
    private Log log;

    private RuntimeCoordinator coordinator;

    private ExtensionHelper extensionHelper;

    public MavenRuntimeBooter(MavenBootConfiguration configuration) {
        outputDirectory = configuration.getOutputDirectory();
        systemConfigDir = configuration.getSystemConfigDir();
        systemConfig = configuration.getSystemConfig();
        bootClassLoader = configuration.getBootClassLoader();
        hostClassLoader = configuration.getHostClassLoader();
        moduleDependencies = configuration.getModuleDependencies();
        extensions = configuration.getExtensions();
        log = configuration.getLog();
        extensionHelper = configuration.getExtensionHelper();
    }

    @SuppressWarnings({"unchecked"})
    public MavenRuntime boot() throws MojoExecutionException {
        try {
            BootstrapService bootstrapService = BootstrapFactory.getService(bootClassLoader);
            MavenRuntime runtime = createRuntime();

            URL systemComposite = bootClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
            Document systemConfig = getSystemConfig(bootstrapService);

            Map<String, String> exportedPackages = new HashMap<String, String>();
            exportedPackages.put("org.fabric3.test.spi", Names.VERSION);
            exportedPackages.put("org.fabric3.runtime.maven", Names.VERSION);

            // process extensions
            List<ContributionSource> contributions = extensionHelper.processExtensions(extensions);

            BootConfiguration configuration = new BootConfiguration();

            List<ComponentRegistration> registrations = new ArrayList<ComponentRegistration>();
            MavenMonitorEventDispatcherFactory factory = new MavenMonitorEventDispatcherFactory(log);
            ComponentRegistration registration = new ComponentRegistration("MonitorEventDispatcherFactory",
                                                                           MonitorEventDispatcherFactory.class,
                                                                           factory, true);
            registrations.add(registration);
            configuration.addRegistrations(registrations);

            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostClassLoader);
            configuration.setBootClassLoader(bootClassLoader);
            configuration.setSystemCompositeUrl(systemComposite);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(contributions);
            configuration.setExportedPackages(exportedPackages);

            coordinator = bootstrapService.createCoordinator(configuration);
            log.info("Starting Fabric3 Runtime ...");
            coordinator.start();
            return runtime;
        } catch (InitializationException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        }
    }

    private MavenRuntime createRuntime() {

        File tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        if (tempDir.exists()) {
            try {
                FileHelper.cleanDirectory(tempDir);
            } catch (IOException e) {
                log.warn("Error cleaning temporary directory: " + e.getMessage());
            }
        }
        tempDir.mkdir();

        URI domain = URI.create(DOMAIN);
        File baseDir = new File(outputDirectory, "test-classes");
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(domain, moduleDependencies, baseDir, tempDir);

        // TODO Add better host JMX support from the next release
        Agent agent = new DefaultAgent();
        MBeanServer mBeanServer = agent.getMBeanServer();

        MavenMonitorEventDispatcher runtimeDispatcher = new MavenMonitorEventDispatcher(log);
        MavenMonitorEventDispatcher appDispatcher = new MavenMonitorEventDispatcher(log);
        RuntimeConfiguration configuration = new RuntimeConfiguration(hostInfo, mBeanServer, runtimeDispatcher, appDispatcher);

        return instantiateRuntime(configuration, bootClassLoader);
    }

    private Document getSystemConfig(BootstrapService bootstrapService) throws MojoExecutionException, InitializationException {
        Source source = null;
        if (systemConfig != null) {
            try {
                InputStream stream = new ByteArrayInputStream(systemConfig.getBytes("UTF-8"));
                source = new InputStreamSource("systemConfig", stream);
            } catch (UnsupportedEncodingException e) {
                throw new MojoExecutionException("Error loading system configuration", e);
            }
        } else {
            URL systemConfig = getSystemConfig();
            if (systemConfig != null) {
                source = new UrlSource(systemConfig);
            }
        }
        Document systemConfig;
        if (source == null) {
            systemConfig = bootstrapService.createDefaultSystemConfig();
        } else {
            systemConfig = bootstrapService.loadSystemConfig(source);
        }
        return systemConfig;
    }

    public void shutdown() throws ShutdownException, InterruptedException, ExecutionException {
        log.info("Stopping Fabric3 Runtime ...");
        coordinator.shutdown();
    }

    private MavenRuntime instantiateRuntime(RuntimeConfiguration configuration, ClassLoader cl) {
        try {
            Class<?> implClass = cl.loadClass(RUNTIME_IMPL);
            return MavenRuntime.class.cast(implClass.getConstructor(RuntimeConfiguration.class).newInstance(configuration));
        } catch (ClassNotFoundException e) {
            // programming error
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // programming error
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            // programming error
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            // programming error
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            // programming error
            throw new AssertionError(e);
        }
    }

    private URL getSystemConfig() throws MojoExecutionException {
        File systemConfig = new File(outputDirectory, DEFAULT_SYSTEM_CONFIG_DIR + SYSTEM_CONFIG_XML_FILE);
        if (systemConfigDir != null) {
            systemConfig = new File(outputDirectory, systemConfigDir + File.separator + SYSTEM_CONFIG_XML_FILE);
            if (!systemConfig.exists()) {
                //The user has explicitly attempted to configure the system config location but the information is incorrect
                throw new MojoExecutionException("Failed to find the system config information in: " + systemConfig.getAbsolutePath());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Using system config information from: " + systemConfig.getAbsolutePath());
        }

        try {
            return systemConfig.exists() ? systemConfig.toURI().toURL() : null;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid system configuration: " + systemConfig, e);
        }
    }


}
