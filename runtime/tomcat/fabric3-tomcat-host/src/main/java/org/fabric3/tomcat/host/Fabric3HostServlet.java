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
package org.fabric3.tomcat.host;

import javax.management.MBeanServer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.mbeans.MBeanUtils;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.RuntimeMode;
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
import org.w3c.dom.Document;
import static org.fabric3.api.host.Names.MONITOR_FACTORY_URI;
import static org.fabric3.api.host.runtime.BootstrapHelper.createHostInfo;

public class Fabric3HostServlet extends HttpServlet implements ContainerServlet {
    private static final long serialVersionUID = 2298727517016404860L;

    private static final String CATALINA_BASE_PROP = "catalina.base";
    private static final String F3_RESTART_PROP = "fabric3.restart";
    private static final String FABRIC3_MODE = "fabric3.mode";
    private static final String FABRIC3_HOME = "fabric3";

    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;
    private Wrapper wrapper;
    private Host host;
    private File installDirectory;
    private boolean restartEnabled;

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
        if (wrapper != null) {
            Context context = (Context) wrapper.getParent();
            host = (Host) context.getParent();
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (host == null) {
            throw new ServletException("Tomcat host not set");
        }
        Engine engine = (Engine) host.getParent();
        init(engine.getService(), true);
    }

    public void destroy() {
        super.destroy();
        stop();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);

        if (!restartEnabled) {
            notAuthorized(resp);
            return;
        }

        if (!hasAdminRole(req, resp)) {
            return;
        }

        boolean restart = Boolean.parseBoolean(req.getParameter("restart"));

        if (restart) {
            destroy();
            Engine engine = (Engine) host.getParent();
            init(engine.getService(), false);
        }

    }

    private synchronized void init(Service service, boolean firstInitTime) throws ServletException {
        try {
            String runtimeId = System.getProperty(FABRIC3_MODE, "vm").toLowerCase();

            restartEnabled = Boolean.parseBoolean(System.getProperty(F3_RESTART_PROP));

            String catalinaInstall = System.getProperty(CATALINA_BASE_PROP);
            File catalinaInstallDir = new File(catalinaInstall);

            if (firstInitTime) {
                installDirectory = findInstallationDirectory(catalinaInstallDir);
            } else if (installDirectory == null) {
                throw new ServletException("No Fabric3 installation found.");
            }

            File extensionsDir = new File(installDirectory, "extensions");

            // calculate config directories based on the mode the runtime is booted in
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
            RuntimeMode mode = bootstrapService.parseRuntimeMode(systemConfig);
            String zoneName = bootstrapService.parseZoneName(systemConfig, mode);

            String environment = bootstrapService.parseEnvironment(systemConfig);

            String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, runtimeId, mode);

            List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

            // create the HostInfo and runtime
            HostInfo hostInfo = createHostInfo(runtimeName, zoneName, mode, domainName, environment, runtimeDir, configDir, extensionsDir, deployDirs, false);

            // clear out the tmp directory
            if (firstInitTime) {
                FileHelper.cleanDirectory(hostInfo.getTempDir());
            }

            // use the Tomcat JMX server
            MBeanServer mBeanServer = MBeanUtils.createServer();

            DelegatingDestinationRouter router = new DelegatingDestinationRouter();

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, router);

            Fabric3Runtime runtime = bootstrapService.createDefaultRuntime(runtimeConfig);

            URL systemComposite = new File(bootDir, "system.composite").toURI().toURL();

            ScanResult result = bootstrapService.scanRepository(hostInfo);

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
            monitor = monitorService.createMonitor(ServerMonitor.class);
            monitor.started(mode.toString(), environment);
        } catch (Fabric3Exception e) {
            if (monitor != null) {
                monitor.runError(e);
            } else {
                e.printStackTrace();
            }
            throw new ServletException(e);
        } catch (IOException e) {
            if (monitor != null) {
                monitor.runError(e);
            } else {
                e.printStackTrace();
            }
            throw new ServletException(e);
        }
    }

    private void stop() {
        try {
            if (coordinator != null) {
                monitor.stopped();
                coordinator.shutdown();
            }
        } catch (ShutdownException e) {
            monitor.runError(e);
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

    private boolean hasAdminRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("restart");
        String password = req.getParameter("password");
        if (username == null || password == null) {
            notAuthenticated(resp);
            return false;
        }
        Realm realm = getWrapper().getRealm();
        if (realm != null) {
            Principal principal = realm.authenticate(username, password);
            if (principal != null) {
                if (!realm.hasRole(principal, "admin-gui") && !realm.hasRole(principal, "admin-script")) {
                    notAuthorized(resp);
                }
            } else {
                notAuthorized(resp);
            }

        } else {
            notAuthorized(resp);
            return false;
        }
        return true;
    }

    private void notAuthenticated(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = resp.getWriter();
        writer.print("No Authentication Credentials");
        writer.close();
    }

    private void notAuthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        PrintWriter writer = resp.getWriter();
        writer.print("Not Authorized");
        writer.close();
    }

    private File findInstallationDirectory(File currentDirectory) {
        if (FABRIC3_HOME.equals(currentDirectory.getName()) && new File(currentDirectory, "host").exists()) {
            return currentDirectory;
        }
        File[] directories = currentDirectory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File dir : directories) {
            File candidate = findInstallationDirectory(dir);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

}
