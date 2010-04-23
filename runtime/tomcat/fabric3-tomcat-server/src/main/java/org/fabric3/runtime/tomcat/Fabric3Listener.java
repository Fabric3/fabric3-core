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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanServer;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.mbeans.MBeanUtils;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.util.FileHelper;

/**
 * Bootstraps Fabric3 in a host Tomcat runtime.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3Listener implements LifecycleListener {
    private static final String HIDE_PACKAGES = "fabric3.hidden.packages";

    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;

    public void lifecycleEvent(LifecycleEvent event) {
        if (!(event.getSource() instanceof StandardServer)) {
            return;
        }
        Server server = (Server) event.getSource();
        if ("after_start".equals(event.getType())) {
            init(server);
        } else if ("stop".equals(event.getType())) {
            stop();
        }
    }

    private void init(Server server) {
        try {
            // This class is loaded in <tomcat install>/lib. The Fabric3 runtime is installed at <tomcat install>/fabric3
            File installDirectory = new File(BootstrapHelper.getInstallDirectory(getClass()), "fabric3");
            //  calculate config directories based on the mode the runtime is booted in
            File configDir = BootstrapHelper.getDirectory(installDirectory, "config");
            // only support single VM mode
            File modeConfigDir = BootstrapHelper.getDirectory(configDir, RuntimeMode.VM.toString().toLowerCase());

            // load properties for this runtime
            File propFile = new File(modeConfigDir, "runtime.properties");
            Properties props = BootstrapHelper.loadProperties(propFile, System.getProperties());

            // create the classloaders for booting the runtime
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");
            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            ClassLoader systemClassLoader = getClass().getClassLoader();
            systemClassLoader = hidePackages(systemClassLoader, props);
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(systemClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            HostInfo hostInfo = BootstrapHelper.createHostInfo(RuntimeMode.VM, installDirectory, configDir, modeConfigDir, props);

            MonitorFactory monitorFactory = createMonitorFactory(configDir, bootLoader);

            // clear out the tmp directory
            FileHelper.cleanDirectory(hostInfo.getTempDir());

            // use the Tomcat JMX server
            MBeanServer mBeanServer = MBeanUtils.createServer();

            RuntimeConfiguration<HostInfo> runtimeConfig = new RuntimeConfiguration<HostInfo>(hostLoader, hostInfo, monitorFactory, mBeanServer);
            Fabric3Runtime<HostInfo> runtime = BootstrapHelper.createDefaultRuntime(runtimeConfig, bootLoader);

            monitor = runtime.getMonitorFactory().getMonitor(ServerMonitor.class);

            Service service = server.findService("Catalina");
            if (service == null) {
                throw new InitializationException("Catalina service not found");
            }

            List<ComponentRegistration> registrations = new ArrayList<ComponentRegistration>();
            ComponentRegistration registration = new ComponentRegistration("CatalinaService", Service.class, service, false);
            registrations.add(registration);

            // boot the runtime
            coordinator = BootstrapHelper.createCoordinator(runtime, Collections.<String, String>emptyMap(), registrations, bootLoader);
            coordinator.start();

            monitor.started(RuntimeMode.VM.toString());
        } catch (Exception e) {
            if (monitor != null) {
                // there could have been an error initializing the monitor
                monitor.runError(e);
            }
            throw new Fabric3ListenerException(e);
        }
    }

    private void stop() {
        try {
            if (coordinator != null) {
                coordinator.shutdown();
            }
            monitor.stopped();
        } catch (ShutdownException ex) {
            monitor.runError(ex);
            throw new Fabric3ListenerException(ex);
        }
    }

    /**
     * Hides JDK and classpath packages from the Fabric3 runtime such as the JAX-WS and JAXB RIs.
     *
     * @param systemClassLoader the system classloader
     * @param props             environment properties
     * @return a classloader that masks packages
     */
    private ClassLoader hidePackages(ClassLoader systemClassLoader, Properties props) {
        // FIXME - Mask Tomcat classes
        String hiddenPackageString = (String) props.get(HIDE_PACKAGES);
        if (hiddenPackageString != null && hiddenPackageString.length() > 0) {
            // mask hidden JDK and system classpath packages
            String[] hiddenPackages = hiddenPackageString.split(",");
            systemClassLoader = new MaskingClassLoader(systemClassLoader, hiddenPackages);
        }
        return systemClassLoader;
    }

    private MonitorFactory createMonitorFactory(File configDir, ClassLoader bootLoader) throws InitializationException, IOException {
        MonitorFactory monitorFactory = BootstrapHelper.createDefaultMonitorFactory(bootLoader);
        File logConfigFile = new File(configDir, "monitor.properties");
        if (logConfigFile.exists()) {
            monitorFactory.readConfiguration(logConfigFile.toURI().toURL());
        }
        return monitorFactory;
    }


}
