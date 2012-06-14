package org.fabric3.tomcat.host;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.mbeans.MBeanUtils;
import org.w3c.dom.Document;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;
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
import static org.fabric3.host.runtime.BootstrapHelper.createHostInfo;

public class Fabric3HostServlet extends HttpServlet implements ContainerServlet {

    private static final String CATALINA_BASE_PROP = "catalina.base";

    private static final long serialVersionUID = 1L;

    private static final String RUNTIME_MODE = "vm";

    private static final String FABRIC3_HOME = "fabric3";

    private RuntimeCoordinator coordinator;
    private ServerMonitor monitor;

    private Wrapper wrapper;

    private Host host;

    private File installDirectory;

    private synchronized void init(Service service, String runtimeId, boolean firstInitTime) {
        try {

            String catalinaInstall = System.getProperty(CATALINA_BASE_PROP);
            File catalinaInstallDir = new File(catalinaInstall);

            if (firstInitTime) {
                installDirectory = findInstallationDirectory(catalinaInstallDir);
            } else if (installDirectory == null) {
                throw new Fabric3HostException("No Fabric3 installation found.");
            }

            File extensionsDir = new File(installDirectory, "extensions");

            // calculate config directories based on the mode the runtime is
            // booted in
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
            HostInfo hostInfo = createHostInfo(runtimeName, mode, domainName, environment, runtimeDir, configDir, extensionsDir, deployDirs);

            // clear out the tmp directory
            if (firstInitTime) {
                FileHelper.cleanDirectory(hostInfo.getTempDir());
            }

            // use the Tomcat JMX server
            MBeanServer mBeanServer = MBeanUtils.createServer();

            // create and configure the monitor dispatchers
            MonitorEventDispatcher runtimeDispatcher = bootstrapService.createMonitorDispatcher(RUNTIME_MONITOR, systemConfig, hostInfo);
            MonitorEventDispatcher appDispatcher = bootstrapService.createMonitorDispatcher(APP_MONITOR, systemConfig, hostInfo);

            RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, runtimeDispatcher, appDispatcher);

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
            monitor = monitorService.createMonitor(ServerMonitor.class, RUNTIME_MONITOR_CHANNEL_URI);
            monitor.started(mode.toString());
        } catch (Exception e) {
            if (monitor != null) {
                // there could have been an error initializing the monitor
                monitor.runError(e);
            }
            throw new Fabric3HostException(e);
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
            throw new Fabric3HostException(ex);
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

    public interface ServerMonitor {

        @Severe("Run error:")
        void runError(Exception e);

        @Info("Fabric3 ready [Mode:{0}]")
        void started(String mode);

        @Info("Fabric3 shutdown")
        void stopped();

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        new Thread(new Runnable() {
            public void run() {
                if (host != null) {
                    Engine engine = (Engine) host.getParent();
                    init(engine.getService(), RUNTIME_MODE, true);
                }
            }
        }).start();
    }

    @Override
    public void destroy() {
        super.destroy();
        stop();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        String restart = req.getParameter("restart");
        String runtime_mode = req.getParameter("runtime_mode");
        String f3_home_path = req.getParameter("fabric3_home");

        runtime_mode = runtime_mode == null ? RUNTIME_MODE : runtime_mode;
        f3_home_path = f3_home_path == null ? FABRIC3_HOME : f3_home_path;

        if (restart != null && Boolean.parseBoolean(restart)) {
            destroy();
            Engine engine = (Engine) host.getParent();
            init(engine.getService(), runtime_mode, false);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
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
            if (candidate != null)
                return candidate;
        }
        return null;
    }

}
