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
package org.fabric3.runtime.weblogic.boot;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fabric3.api.host.Names;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.classloader.MaskingClassLoader;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.BootstrapFactory;
import org.fabric3.api.host.runtime.BootstrapHelper;
import org.fabric3.api.host.runtime.BootstrapService;
import org.fabric3.api.host.runtime.ComponentRegistration;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.runtime.ScanResult;
import org.fabric3.api.host.runtime.ShutdownException;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.runtime.weblogic.api.Constants;
import org.fabric3.runtime.weblogic.monitor.WebLogicDestinationRouter;
import org.fabric3.runtime.weblogic.work.WebLogicExecutorService;
import org.w3c.dom.Document;
import static org.fabric3.api.host.Names.MONITOR_FACTORY_URI;
import static org.fabric3.runtime.weblogic.api.Constants.RUNTIME_ATTRIBUTE;

/**
 * Bootstraps the Fabric3 runtime in WebLogic Server.
 */
public class Fabric3WebLogicListener implements ServletContextListener {
    private static final String FABRIC3_HOME = "fabric3.home";
    private static final String FABRIC3_MODE = "fabric3.mode";
    private static final String FABRIC3_WEBLOGIC_HOST = "fabric3-weblogic-host";

    // The WLS application activated state. Other WLS states:  UNPREPARED = 0; PREPARED = 1; NEW = 3; UPDATE_PENDING = 4
    private static final int WLS_ACTIVATED_STATE = 2;

    private ServletContext context;
    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;

    public void contextInitialized(ServletContextEvent event) {
        try {
            context = event.getServletContext();
            RuntimeMode runtimeMode = getRuntimeMode();
            MBeanServer mBeanServer = getMBeanServer();
            ObjectName componentRuntime = getComponentRuntimeMBean(mBeanServer);

            String pathName = System.getProperty(FABRIC3_HOME);
            if (pathName == null) {
                event.getServletContext().log("fabric3.home system property not specified");
                return;
            }
            File installDirectory = new File(pathName);
            if (!installDirectory.exists()) {
                event.getServletContext().log("fabric3.home directory does not exist: " + pathName);
                return;
            }
            prepare(runtimeMode, mBeanServer, installDirectory, componentRuntime);
        } catch (NamingException | JMException e) {
            context.log("Error initializing Fabric3", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (coordinator == null) {
                return;
            }
            coordinator.shutdown();
            if (monitor != null) {
                monitor.stopped();
            }
        } catch (ShutdownException e) {
            context.log("Error shutting down Fabric3", e);
        }
    }

    /**
     * Prepares the runtime.
     *
     * @param runtimeMode      the mode to start the runtime in
     * @param mBeanServer      the WebLogic runtime mBeanServer
     * @param installDirectory the directory containing the Fabric3 runtime image
     * @param componentRuntime the component runtime MBean used to determine the state of the F3 host web application
     */
    public void prepare(RuntimeMode runtimeMode, MBeanServer mBeanServer, File installDirectory, ObjectName componentRuntime) {

        // override EclipseLink JAXB with the Sun JAXB RI
        System.setProperty(JAXBContext.class.getName(), "com.sun.xml.bind.v2.ContextFactory");

        String vm = System.getProperty("java.vm.name");
        if (vm != null && vm.contains("IBM J9 VM")) {
            // J9 does not contain the Sun Xerxes implementation which is referenced by libraries such as Metro WS
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            System.setProperty("javax.xml.transform.TransformerFactory", "com.ibm.xtq.xslt.jaxp.compiler.TransformerFactoryImpl");
            System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            //  calculate config directories based on the mode the runtime is booted in
            String runtimeId;
            String runtimeDirName;
            String runtimeName;
            if (RuntimeMode.CONTROLLER == runtimeMode) {
                runtimeId = "controller";
                runtimeName = "controller";
                runtimeDirName = runtimeId;
            } else if (RuntimeMode.PARTICIPANT == runtimeMode) {
                runtimeId = getRuntimeId(mBeanServer);
                runtimeName = runtimeId;
                runtimeDirName = "participant";
            } else {
                runtimeId = getRuntimeId(mBeanServer);
                runtimeName = runtimeId;
                runtimeDirName = "vm";
            }
            File rootRuntimeDir = BootstrapHelper.getDirectory(installDirectory, "runtimes");
            File runtimeDir = new File(rootRuntimeDir, runtimeDirName);

            File configDir = BootstrapHelper.getDirectory(runtimeDir, "config");

            File extensionsDir = new File(installDirectory, "extensions");

            // create the classloaders for booting the runtime
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");

            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            // set the context classloader to the host classloader
            ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();

            ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, WebLogicHiddenPackages.getPackages(), true);
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

            // load the system configuration
            Document systemConfig = bootstrapService.loadSystemConfig(configDir);

            URI domainName = bootstrapService.parseDomainName(systemConfig);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

            // create the HostInfo and runtime
            HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName,
                                                               Names.DEFAULT_ZONE,
                                                               runtimeMode,
                                                               domainName,
                                                               environment,
                                                               runtimeDir, extensionsDir,
                                                               deployDirs,
                                                               true);

            // clear out the tmp directory
            FileHelper.cleanDirectory(hostInfo.getTempDir());

            WebLogicDestinationRouter destinationRouter = new WebLogicDestinationRouter();

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, destinationRouter);

            Fabric3Runtime runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            Thread.currentThread().setContextClassLoader(hostLoader);

            Map<String, String> exportedPackages = getExportedPackages();

            ScanResult result = bootstrapService.scanRepository(hostInfo);

            BootConfiguration configuration = new BootConfiguration();

            List<ComponentRegistration> registrations = new ArrayList<>();

            WebLogicExecutorService executorService = new WebLogicExecutorService();
            ComponentRegistration executorRegistration = new ComponentRegistration("WebLogicExecutorService", ExecutorService.class, executorService, true);
            registrations.add(executorRegistration);

            configuration.addRegistrations(registrations);

            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostLoader);
            configuration.setBootClassLoader(bootLoader);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(result.getExtensionContributions());
            configuration.setUserContributions(result.getUserContributions());
            configuration.setExportedPackages(exportedPackages);
            configuration.setHostCapabilities(getHostCapabilities());

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.boot();
            coordinator.load();
            context.setAttribute(RUNTIME_ATTRIBUTE, runtime);

            MonitorProxyService monitorService = runtime.getComponent(MonitorProxyService.class, MONITOR_FACTORY_URI);
            monitor = monitorService.createMonitor(ServerMonitor.class);

            start(mBeanServer, componentRuntime, runtimeMode);

        } catch (Exception e) {
            context.log("Error initializing Fabric3", e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Starts the runtime. If the host web application is in the active state, the runtime will be started synchronously. If not, the runtime will be started on
     * a separate thread after the web application state has been transitioned to active. Starting the runtime asynchronously avoids race conditions where a
     * deployment is sent to participants and the host web app is not initialized prior to participants attempting to resolve contributions (.cf
     * FABRICTHREE-662).
     *
     * @param mBeanServer      the MBean server
     * @param componentRuntime the component runtime MBean
     * @param runtimeMode      the runtime mode
     */
    public void start(final MBeanServer mBeanServer, final ObjectName componentRuntime, final RuntimeMode runtimeMode) {
        try {
            int state = (Integer) mBeanServer.getAttribute(componentRuntime, "DeploymentState");

            // If the deployment state is activated, start immediately. Otherwise, do so asynchronously.
            // Note that an MBean NotificationListener cannot be used as the WLS MBean does not emmit notifications.
            if (WLS_ACTIVATED_STATE == state) {
                coordinator.joinDomain();
                monitor.started(runtimeMode.toString());
            } else {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                int state = (Integer) mBeanServer.getAttribute(componentRuntime, "DeploymentState");
                                if (WLS_ACTIVATED_STATE == state) {
                                    coordinator.joinDomain();
                                    monitor.started(runtimeMode.toString());
                                    return;
                                }
                                Thread.sleep(1000); // wait a second and retry
                            } catch (JMException e) {
                                monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
                            } catch (InterruptedException e) {
                                Thread.interrupted();
                            }
                        }
                    }
                });
            }
        } catch (JMException e) {
            monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
        }
    }

    private ObjectName getComponentRuntimeMBean(MBeanServer mBeanServer) throws JMException {
        // lookup the component runtime MBean containing the current app deployment state
        ObjectName serverRuntime = (ObjectName) mBeanServer.getAttribute(Constants.WLS_RUNTIME_SERVICE_MBEAN, "ServerRuntime");
        ObjectName[] applicationRuntimes = (ObjectName[]) mBeanServer.getAttribute(serverRuntime, "ApplicationRuntimes");
        ObjectName applicationRuntime = null;
        for (ObjectName runtime : applicationRuntimes) {
            if (runtime.getKeyProperty("Name").contains(FABRIC3_WEBLOGIC_HOST)) {
                applicationRuntime = runtime;
                break;
            }
        }
        if (applicationRuntime == null) {
            monitor.errorMessage("Application runtime MBean not found. Federation and cluster communication disabled.");
            return null;
        }
        ObjectName[] componentRuntimes = ((ObjectName[]) mBeanServer.getAttribute(applicationRuntime, "ComponentRuntimes"));
        ObjectName componentRuntime = null;
        for (ObjectName runtime : componentRuntimes) {
            if (runtime.getKeyProperty("ApplicationRuntime").contains(FABRIC3_WEBLOGIC_HOST)) {
                componentRuntime = runtime;
                break;
            }
        }
        if (componentRuntime == null) {
            monitor.errorMessage("Component runtime MBean not found. Federation and cluster communication disabled.");
            return null;
        }
        return componentRuntime;
    }

    private Map<String, String> getExportedPackages() {
        Map<String, String> exportedPackages = new HashMap<>();
        exportedPackages.put("org.fabric3.spi.*", Names.VERSION);
        exportedPackages.put("com.bea.core.workmanager", "1.7.0.0");
        exportedPackages.put("com.bea.core.workmanager.internal", "1.7.0.0");
        exportedPackages.put("weblogic.common", "1.7.0.0");
        exportedPackages.put("weblogic.kernel", "1.7.0.0");
        exportedPackages.put("weblogic.work", "1.7.0.0");
        exportedPackages.put("weblogic.work.commonj", "1.7.0.0");
        exportedPackages.put("javax.jms", "1.1.0");
        exportedPackages.put("javax.transaction", "1.1.0");
        exportedPackages.put("javax.transaction.xa", "1.1.0");
        exportedPackages.put("org.fabric3.runtime.weblogic.api", Names.VERSION);
        return exportedPackages;
    }

    private List<String> getHostCapabilities() {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("transaction");
        return capabilities;
    }

    private static RuntimeMode getRuntimeMode() {
        // TODO implement by introspecting MBeans
        String mode = System.getProperty(FABRIC3_MODE, "vm");
        if ("controller".equals(mode)) {
            return RuntimeMode.CONTROLLER;
        } else if ("participant".equals(mode)) {
            return RuntimeMode.PARTICIPANT;
        } else if (!"vm".equals(mode)) {
            throw new IllegalArgumentException("Invalid runtime mode: " + mode + ". Valid modes are 'controller', 'participant' or 'vm' (default).");
        }
        return RuntimeMode.VM;
    }

    private MBeanServer getMBeanServer() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (MBeanServer) ctx.lookup("java:comp/env/jmx/runtime");
    }

    public String getRuntimeId(MBeanServer mbServer) throws JMException {
        Object current = Constants.WLS_RUNTIME_SERVICE_MBEAN;
        String[] path = {"ServerRuntime", "Name"};
        for (String token : path) {
            current = mbServer.getAttribute((ObjectName) current, token);
        }
        return (String) current;
    }

}
