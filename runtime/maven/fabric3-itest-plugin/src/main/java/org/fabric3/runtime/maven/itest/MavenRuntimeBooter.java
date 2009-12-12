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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.xml.sax.InputSource;

import org.fabric3.host.Names;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.jmx.agent.ManagementException;
import org.fabric3.runtime.maven.MavenRuntime;
import org.fabric3.util.io.FileHelper;

/**
 * @version $Rev$ $Date$
 */
public class MavenRuntimeBooter {
    private static final String SYSTEM_CONFIG_XML_FILE = "systemConfig.xml";
    private static final String DEFAULT_SYSTEM_CONFIG_DIR = "test-classes" + File.separator + "META-INF" + File.separator;
    private static final String RUNTIME_IMPL = "org.fabric3.runtime.maven.impl.MavenRuntimeImpl";
    private static final String BOOTSTRAPPER_IMPL = "org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl";
    private static final String COORDINATOR_IMPL = "org.fabric3.fabric.runtime.DefaultCoordinator";
    private static final String DOMAIN = "fabric3://domain";

    // configuration elements
    private URL systemScdl;
    private Properties properties;
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private Set<org.apache.maven.model.Dependency> extensions;
    private Log log;


    private RuntimeLifecycleCoordinator coordinator;
    private MavenRuntime runtime;
    private ExtensionHelper extensionHelper;

    public MavenRuntimeBooter(MavenBootConfiguration configuration) {
        systemScdl = configuration.getSystemScdl();
        properties = configuration.getProperties();
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
        runtime = createRuntime();
        BootConfiguration configuration = createBootConfiguration();
        coordinator = instantiate(RuntimeLifecycleCoordinator.class, COORDINATOR_IMPL, bootClassLoader);
        coordinator.setConfiguration(configuration);
        bootRuntime();
        return runtime;
    }

    private MavenRuntime createRuntime() throws MojoExecutionException {
        MonitorFactory monitorFactory = new MavenMonitorFactory(log);
        MavenRuntime runtime = instantiate(MavenRuntime.class, RUNTIME_IMPL, bootClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostClassLoader(hostClassLoader);

        Properties hostProperties = properties != null ? properties : System.getProperties();
        File tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        if (tempDir.exists()) {
            try {
                FileHelper.cleanDirectory(tempDir);
            } catch (IOException e) {
                throw new MojoExecutionException("Error cleaning temporary directory", e);
            }
        }
        tempDir.mkdir();

        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(DOMAIN), hostProperties, moduleDependencies, tempDir);
        runtime.setHostInfo(hostInfo);

        // TODO Add better host JMX support from the next release
        Agent agent;
        try {
            agent = new DefaultAgent();
        } catch (ManagementException e) {
            throw new MojoExecutionException("Error initializing JMX Agent", e);
        }
        runtime.setMBeanServer(agent.getMBeanServer());

        return runtime;
    }

    private BootConfiguration createBootConfiguration() throws MojoExecutionException {

        BootConfiguration configuration = new BootConfiguration();
        configuration.setBootClassLoader(bootClassLoader);

        // create the runtime bootrapper
        ScdlBootstrapper bootstrapper = createBootstrapper(bootClassLoader);
        configuration.setBootstrapper(bootstrapper);

        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.test.spi", Names.VERSION);
        exportedPackages.put("org.fabric3.runtime.maven", Names.VERSION);
        configuration.setExportedPackages(exportedPackages);
        // process extensions
        extensionHelper.processExtensions(configuration, extensions);

        configuration.setRuntime(runtime);

        return configuration;
    }

    private ScdlBootstrapper createBootstrapper(ClassLoader bootClassLoader) throws MojoExecutionException {
        ScdlBootstrapper bootstrapper = instantiate(ScdlBootstrapper.class, BOOTSTRAPPER_IMPL, bootClassLoader);
        if (systemScdl == null) {
            systemScdl = bootClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
        }
        bootstrapper.setScdlLocation(systemScdl);
        if (systemConfig != null) {
            Reader reader = new StringReader(systemConfig);
            InputSource source = new InputSource(reader);
            bootstrapper.setSystemConfig(source);
        } else {
            URL systemConfig = getSystemConfig();
            bootstrapper.setSystemConfig(systemConfig);
        }
        return bootstrapper;
    }

    private void bootRuntime() throws MojoExecutionException {
        try {
            log.info("Starting Fabric3 Runtime ...");
            coordinator.bootPrimordial();
            coordinator.initialize();
            coordinator.recover();
            coordinator.joinDomain(-1);
            coordinator.start();
        } catch (InitializationException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        }
    }

    public void shutdown() throws ShutdownException, InterruptedException, ExecutionException {
        log.info("Stopping Fabric3 Runtime ...");
        coordinator.shutdown();
    }

    private <T> T instantiate(Class<T> type, String impl, ClassLoader cl) {
        try {
            Class<?> implClass = cl.loadClass(impl);
            return type.cast(implClass.newInstance());
        } catch (ClassNotFoundException e) {
            // programming errror
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // programming errror
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            // programming errror
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
            return systemConfig.exists() ? systemConfig.toURL() : null;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid system configuration: " + systemConfig, e);
        }
    }


}
