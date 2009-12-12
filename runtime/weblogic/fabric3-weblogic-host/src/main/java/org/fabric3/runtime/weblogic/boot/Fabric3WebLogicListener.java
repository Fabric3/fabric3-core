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
package org.fabric3.runtime.weblogic.boot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.host.runtime.RepositoryScanner;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.runtime.weblogic.monitor.WebLogicMonitorFactory;

/**
 * Bootstraps the Fabric3 runtime in WebLogic Server.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3WebLogicListener implements ServletContextListener {
    private static final String FABRIC3_HOME = "fabric3.home";
    private static final String HIDE_PACKAGES = "fabric3.hidden.packages";
    private ServletContext context;
    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;


    public void contextInitialized(ServletContextEvent event) {
        try {
            context = event.getServletContext();
            RuntimeMode runtimeMode = getRuntimeMode();
            MBeanServer mBeanServer = getMBeanServer();
            String pathname = System.getProperty(FABRIC3_HOME);
            if (pathname == null) {
                event.getServletContext().log("fabric3.home system property not specified");
                return;
            }
            File installDirectory = new File(pathname);
            if (!installDirectory.exists()) {
                event.getServletContext().log("fabric3.home directory does not exist: " + pathname);
                return;
            }
            start(runtimeMode, mBeanServer, installDirectory);
        } catch (NamingException e) {
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
     * Starts the runtime in a blocking fashion and only returns after it has been released from another thread.
     *
     * @param runtimeMode      the mode to start the runtime in
     * @param mBeanServer      the WebLogic runtime mBeanServer
     * @param installDirectory the directory containing the Fabric3 runtime image
     */
    public void start(RuntimeMode runtimeMode, MBeanServer mBeanServer, File installDirectory) {

        try {
            //  calculate config directories based on the mode the runtime is booted in
            File configDir = BootstrapHelper.getDirectory(installDirectory, "config");
            File modeConfigDir = BootstrapHelper.getDirectory(configDir, runtimeMode.toString().toLowerCase());

            // load properties for this runtime
            File propFile = new File(modeConfigDir, "runtime.properties");
            Properties props = BootstrapHelper.loadProperties(propFile, System.getProperties());

            // create the classloaders for booting the runtime
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");

            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();
            String hiddenPackageString = (String) props.get(HIDE_PACKAGES);
            if (hiddenPackageString != null && hiddenPackageString.length() > 0) {
                // mask hidden JDK and system classpath packages
                String[] hiddenPackages = hiddenPackageString.split(",");
                systemClassLoader = new MaskingClassLoader(systemClassLoader, hiddenPackages);
            }
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(systemClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            // create the HostInfo, MonitorFactory, and runtime
            HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeMode, installDirectory, configDir, modeConfigDir, props);

            MonitorFactory monitorFactory = new WebLogicMonitorFactory();
            Fabric3Runtime<HostInfo> runtime = BootstrapHelper.createRuntime(hostInfo, hostLoader, bootLoader, monitorFactory);

            monitor = runtime.getMonitorFactory().getMonitor(ServerMonitor.class);

            runtime.setMBeanServer(mBeanServer);

            // boot the runtime
            coordinator = BootstrapHelper.createCoordinator(bootLoader);
            BootConfiguration configuration = createBootConfiguration(runtime, bootLoader);
            coordinator.setConfiguration(configuration);
            coordinator.start();

            monitor.started(runtimeMode.toString());
        } catch (RuntimeException e) {
            context.log("Error initializing Fabric3", e);
        } catch (Exception e) {
            context.log("Error initializing Fabric3", e);
        }
    }

    private BootConfiguration createBootConfiguration(Fabric3Runtime<HostInfo> runtime, ClassLoader bootClassLoader) throws InitializationException {
        HostInfo hostInfo = runtime.getHostInfo();
        BootConfiguration configuration = new BootConfiguration();
        configuration.setBootClassLoader(bootClassLoader);
        configuration.setRuntime(runtime);

        // create the runtime bootrapper
        Bootstrapper bootstrapper = BootstrapHelper.createBootstrapper(hostInfo, bootClassLoader);
        configuration.setBootstrapper(bootstrapper);

        // process extensions
        File repositoryDirectory = hostInfo.getRepositoryDirectory();
        RepositoryScanner scanner = new RepositoryScanner();
        ScanResult result = scanner.scan(repositoryDirectory);
        configuration.setExtensionContributions(result.getExtensionContributions());
        configuration.setUserContributions(result.getUserContributions());

        setExportedPackages(configuration);
        return configuration;
    }

    private void setExportedPackages(BootConfiguration configuration) {
        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.spi.*", "1.4");
        exportedPackages.put("com.bea.core.workmanager", "1.7.0.0");
        exportedPackages.put("com.bea.core.workmanager.internal", "1.7.0.0");
        exportedPackages.put("weblogic.common", "1.7.0.0");
        exportedPackages.put("weblogic.kernel", "1.7.0.0");
        exportedPackages.put("weblogic.work", "1.7.0.0");
        exportedPackages.put("weblogic.work.commonj", "1.7.0.0");
        exportedPackages.put("org.fabric3.runtime.weblogic.api", Names.VERSION);
        configuration.setExportedPackages(exportedPackages);
    }


    private static RuntimeMode getRuntimeMode() {
        // TODO implement by introspecting MBeans
        return RuntimeMode.VM;
    }

    private MBeanServer getMBeanServer() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (MBeanServer) ctx.lookup("java:comp/env/jmx/runtime");
        // TODO create the runtime and put it in the servlet context
    }

    public interface ServerMonitor {
        @Severe
        void runError(Exception e);

        @Info
        void started(String mode);

        @Info
        void stopped();

        @Info
        void exited(Exception e);

    }

}
