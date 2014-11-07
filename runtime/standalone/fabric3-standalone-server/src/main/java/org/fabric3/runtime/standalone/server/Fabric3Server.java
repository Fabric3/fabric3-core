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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.standalone.server;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.classloader.MaskingClassLoader;
import org.fabric3.api.host.monitor.DelegatingDestinationRouter;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.runtime.BootConfiguration;
import org.fabric3.api.host.runtime.BootstrapFactory;
import org.fabric3.api.host.runtime.BootstrapHelper;
import org.fabric3.api.host.runtime.BootstrapService;
import org.fabric3.api.host.runtime.ComponentRegistration;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HiddenPackages;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.RuntimeCoordinator;
import org.fabric3.api.host.runtime.ScanResult;
import org.fabric3.api.host.runtime.ShutdownException;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.model.type.RuntimeMode;
import org.w3c.dom.Document;
import static org.fabric3.api.host.Names.MONITOR_FACTORY_URI;

/**
 * This class provides the command line interface for starting the Fabric3 standalone server.
 */
public class Fabric3Server implements Fabric3ServerMBean {
    private static final String DOMAIN = "fabric3";
    private static final String RUNTIME_MBEAN = "fabric3:SubDomain=runtime, type=component, name=RuntimeMBean";

    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;
    private CountDownLatch latch;
    private String productName;

    /**
     * Main method.
     *
     * @param args command line arguments.
     * @throws Fabric3Exception if there is a catastrophic problem starting the runtime
     */
    public static void main(String[] args) throws Fabric3Exception {
        Params params = parse(args);
        Fabric3Server server = new Fabric3Server();
        server.start(params);
        System.exit(0);
    }

    /**
     * Starts the runtime in a blocking fashion and only returns after it has been released from another thread.
     *
     * @param params the runtime parameters
     * @throws Fabric3ServerException if catastrophic exception was encountered leaving the runtime in an unstable state
     */
    public void start(Params params) throws Fabric3ServerException {

        DelegatingDestinationRouter router = new DelegatingDestinationRouter();

        try {
            //  calculate config directories based on the mode the runtime is booted in
            File installDirectory = BootstrapHelper.getInstallDirectory(Fabric3Server.class);
            File extensionsDir = new File(installDirectory, "extensions");
            File runtimeDir = getRuntimeDirectory(params, installDirectory);

            File configDir = BootstrapHelper.getDirectory(runtimeDir, "config");
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");
            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            // create the classloaders for booting the runtime
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader maskingClassLoader = new MaskingClassLoader(systemClassLoader, HiddenPackages.getPackages());
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

            // load the system configuration
            Document systemConfig = bootstrapService.loadSystemConfig(configDir);

            URI domainName = bootstrapService.parseDomainName(systemConfig);

            RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            String zoneName = bootstrapService.parseZoneName(systemConfig, mode);

            productName = bootstrapService.parseProductName(systemConfig);

            String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, params.name, mode);

            List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

            // create the HostInfo and runtime
            HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName,
                                                               zoneName,
                                                               mode,
                                                               domainName,
                                                               environment,
                                                               runtimeDir,
                                                               extensionsDir,
                                                               deployDirs,
                                                               false);

            // clear out the tmp directory
            FileHelper.cleanDirectory(hostInfo.getTempDir());

            // clean if set
            if (params.clean) {
                File dataDir = BootstrapHelper.getDirectory(runtimeDir, "data");
                FileHelper.cleanDirectory(dataDir);
            }

            MBeanServer mbServer = MBeanServerFactory.createMBeanServer(DOMAIN);

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mbServer, router, null);

            Fabric3Runtime runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            ScanResult result = bootstrapService.scanRepository(hostInfo);

            BootConfiguration configuration = new BootConfiguration();
            configuration.setRuntime(runtime);
            configuration.setHostClassLoader(hostLoader);
            configuration.setBootClassLoader(bootLoader);
            configuration.setSystemConfig(systemConfig);
            configuration.setExtensionContributions(result.getExtensionContributions());
            configuration.setUserContributions(result.getUserContributions());

            List<ComponentRegistration> registrations = bootstrapService.createDefaultRegistrations(runtime);
            configuration.addRegistrations(registrations);

            // start the runtime
            coordinator = bootstrapService.createCoordinator(configuration);
            coordinator.start();

            // register the runtime with the MBean server
            ObjectName objectName = new ObjectName(RUNTIME_MBEAN);
            mbServer.registerMBean(this, objectName);

            // create the shutdown daemon
            latch = new CountDownLatch(1);

            MonitorProxyService monitorService = runtime.getComponent(MonitorProxyService.class, MONITOR_FACTORY_URI);
            monitor = monitorService.createMonitor(ServerMonitor.class);
            monitor.started(productName, mode.toString(), environment);

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            router.flush(System.out);
            shutdown();
            handleStartException(ex);
        }
    }

    public void shutdownRuntime() {
        shutdown();
        latch.countDown();
    }

    private void shutdown() {
        try {
            if (coordinator != null) {
                if (monitor != null) {
                    monitor.shutdown(productName);
                }
                coordinator.shutdown();
            }
        } catch (ShutdownException ex) {
            if (monitor != null) {
                monitor.shutdownError(ex);
            } else {
                ex.printStackTrace();
            }
        }
    }

    private File getRuntimeDirectory(Params params, File installDirectory) throws Fabric3ServerException, IOException {
        File rootRuntimeDir;
        if (params.directory != null) {
            rootRuntimeDir = params.directory;
        } else {
            rootRuntimeDir = new File(installDirectory, "runtimes");
        }
        File runtimeDir = new File(rootRuntimeDir, params.name);
        if (!runtimeDir.exists()) {
            if (params.clone != null) {
                File templateDir = BootstrapHelper.getDirectory(rootRuntimeDir, params.clone);
                File configDir = BootstrapHelper.getDirectory(templateDir, "config");
                if (!configDir.exists()) {
                    throw new Fabric3ServerException("Unable to create runtime directory: " + runtimeDir);
                }
                BootstrapHelper.cloneRuntimeImage(configDir, runtimeDir);
            } else {
                throw new IllegalArgumentException("Runtime directory does not exist:" + runtimeDir);
            }
        }
        return runtimeDir;
    }

    private void handleStartException(Exception ex) {
        if (monitor != null) {
            // there could have been an error initializing the monitor
            monitor.exited(ex);
        } else {
            ex.printStackTrace();
        }
    }

    private static Params parse(String[] args) {
        Params params = new Params();
        for (String arg : args) {
            if (arg.startsWith("name:")) {
                params.name = arg.substring(5);
            } else if (arg.startsWith("dir:")) {
                params.directory = new File(arg.substring(4));
            } else if (arg.startsWith("clone:")) {
                params.clone = arg.substring(6);
            } else if (arg.equals("clean")) {
                params.clean = true;
            } else if (!arg.contains(":")) {
                // assume this is the runtime name
                params.name = arg;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        if (params.name == null) {
            // default to VM
            params.name = "vm";
        }
        return params;
    }

    private static class Params {
        String name;
        File directory;
        String clone;
        public boolean clean;
    }

    public interface ServerMonitor {

        @Severe("Shutdown error")
        void shutdownError(Exception e);

        @Info("{0} ready [Mode:{1}, Environment: {2}]")
        void started(String mode, String environment, String s);

        @Info("{0} shutting down")
        void shutdown(String productName);

        @Info("Runtime exited abnormally, Caused by")
        void exited(Exception e);

    }

}
