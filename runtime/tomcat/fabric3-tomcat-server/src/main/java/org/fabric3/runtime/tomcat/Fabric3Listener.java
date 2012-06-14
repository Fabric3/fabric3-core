/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServer;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.mbeans.MBeanUtils;
import org.w3c.dom.Document;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapFactory;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HiddenPackages;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.util.FileHelper;

import static org.fabric3.host.Names.MONITOR_FACTORY_URI;
import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL_URI;
import static org.fabric3.host.runtime.BootConstants.APP_MONITOR;
import static org.fabric3.host.runtime.BootConstants.RUNTIME_MONITOR;

/**
 * Bootstraps Fabric3 in a host Tomcat runtime.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3Listener implements LifecycleListener {
    private static final String RUNTIME_NAME = "org.fabric3.name";

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
            String runtimeId = System.getProperty(RUNTIME_NAME, "vm");

            // This class is loaded in <tomcat install>/lib. The Fabric3 runtime is installed at <tomcat install>/fabric3
            File installDirectory = new File(BootstrapHelper.getInstallDirectory(getClass()), "fabric3");
            File extensionsDir = new File(installDirectory, "extensions");

            //  calculate config directories based on the mode the runtime is booted in
            File runtimeDir = getRuntimeDirectory(installDirectory, runtimeId);
            File configDir = BootstrapHelper.getDirectory(runtimeDir, "config");

            // create the classloaders for booting the runtime
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");
            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            ClassLoader systemClassLoader = getClass().getClassLoader();
            ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, HiddenPackages.getPackages());
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

            // load the system configuration
            Document systemConfig = bootstrapService.loadSystemConfig(configDir);

            URI domainName = bootstrapService.parseDomainName(systemConfig);
            String zoneName = bootstrapService.parseZoneName(systemConfig);
            RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, runtimeId, mode);

            List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

            // create the HostInfo and runtime
            HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName,
                                                               mode,
                                                               domainName,
                                                               environment,
                                                               runtimeDir,
                                                               configDir,
                                                               extensionsDir,
                                                               deployDirs);

            // clear out the tmp directory
            FileHelper.cleanDirectory(hostInfo.getTempDir());

            // use the Tomcat JMX server
            MBeanServer mBeanServer = MBeanUtils.createServer();

            // create and configure the monitor dispatchers
            MonitorEventDispatcher runtimeDispatcher = bootstrapService.createMonitorDispatcher(RUNTIME_MONITOR, systemConfig, hostInfo);
            MonitorEventDispatcher appDispatcher = bootstrapService.createMonitorDispatcher(APP_MONITOR, systemConfig, hostInfo);

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, runtimeDispatcher, appDispatcher);

            Fabric3Runtime runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            URL systemComposite = new File(bootDir, "system.composite").toURI().toURL();

            ScanResult result = bootstrapService.scanRepository(hostInfo);

            Service service = server.findService("Catalina");
            if (service == null) {
                throw new InitializationException("Catalina service not found");
            }

            List<ComponentRegistration> registrations = new ArrayList<ComponentRegistration>();
            ComponentRegistration registration = new ComponentRegistration("CatalinaService", Service.class, service, false);
            registrations.add(registration);

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostLoader);
            configuration.setBootClassLoader(bootLoader);
            configuration.setSystemCompositeUrl(systemComposite);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(result.getExtensionContributions());
            configuration.setUserContributions(result.getUserContributions());
            configuration.addRegistrations(registrations);

            // boot the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.start();
            MonitorProxyService monitorService = runtime.getComponent(MonitorProxyService.class, MONITOR_FACTORY_URI);
            monitor = monitorService.createMonitor(ServerMonitor.class, RUNTIME_MONITOR_CHANNEL_URI);
            monitor.started(mode.toString());
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
                monitor.stopped();
                coordinator.shutdown();
            }
        } catch (ShutdownException ex) {
            monitor.runError(ex);
            throw new Fabric3ListenerException(ex);
        }
    }

    private File getRuntimeDirectory(File installDirectory, String runtimeName) {
        File rootRuntimeDir = new File(installDirectory, "runtimes");
        File runtimeDir = new File(rootRuntimeDir, runtimeName);
        if (!runtimeDir.exists()) {
            throw new IllegalArgumentException("Runtime directory does not exist:" + runtimeDir);
        }
        return runtimeDir;
    }

}
