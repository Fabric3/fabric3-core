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
package org.fabric3.transport.jetty.impl;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.jsp.JettyLog;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.federation.FederationConstants;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.KeyStoreManager;
import org.fabric3.spi.transport.Transport;
import org.fabric3.transport.jetty.JettyService;
import org.fabric3.transport.jetty.management.ManagedHashSessionManager;
import org.fabric3.transport.jetty.management.ManagedServletHandler;
import org.fabric3.transport.jetty.management.ManagedServletHolder;
import org.fabric3.transport.jetty.management.ManagedStatisticsHandler;

/**
 * Implements an HTTP transport service using Jetty.
 *
 * @version $$Rev$$ $$Date$$
 */
@EagerInit
@Service(interfaces = {JettyService.class, Transport.class})
public class JettyServiceImpl implements JettyService, Transport {
    private static final String ORG_ECLIPSE_JETTY_UTIL_LOG_CLASS = "org.eclipse.jetty.util.log.class";
    private static final String HTTP_SERVLETS = "HTTP/servlets";

    private static final String ROOT = "/";

    private ExecutorService executorService;
    private ManagementService managementService;
    private HostInfo hostInfo;
    private TransportMonitor monitor;

    private ZoneTopologyService topologyService;

    private KeyStoreManager keyStoreManager;
    private AuthenticationService authenticationService;

    private final Object joinLock = new Object();
    private boolean enableHttps;
    private int minHttpPort = 8080;
    private int maxHttpPort = -1;
    private int selectedHttp = -1;
    private int minHttpsPort = 8484;
    private int maxHttpsPort = -1;
    private int selectedHttps = -1;
    //    private String keystore;
    private boolean sendServerVersion;
    private boolean debug;
    private Server server;
    private ManagedServletHandler servletHandler;
    private SelectChannelConnector httpConnector;
    private ContextAwareSslConnector sslConnector;

    private ContextHandlerCollection rootHandler;
    private ManagedStatisticsHandler statisticsHandler;
    private ManagedHashSessionManager sessionManager;
    private ServletContextHandler contextHandler;


    static {
        // replace the static Jetty logger
        System.setProperty(ORG_ECLIPSE_JETTY_UTIL_LOG_CLASS, JettyLogger.class.getName());
    }

    @Constructor
    public JettyServiceImpl(@Reference ExecutorService executorService,
                            @Reference ManagementService managementService,
                            @Reference HostInfo hostInfo,
                            @Monitor TransportMonitor monitor) {
        this.executorService = executorService;
        this.managementService = managementService;
        this.hostInfo = hostInfo;
        this.monitor = monitor;
        // Re-route the Jetty logger to use a monitor
        JettyLogger.setMonitor(monitor);
        if (debug) {
            JettyLogger.enableDebug();
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Re-route the GlassFish JSP logger. The GlassFish JSP engine is used by Jetty.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            JettyLog.init();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public JettyServiceImpl(TransportMonitor monitor, HostInfo hostInfo) {
        this.monitor = monitor;
        this.hostInfo = hostInfo;
    }

    @Reference(required = false)
    public void setKeyStoreManager(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager;
    }

    @Reference(required = false)
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Reference(required = false)
    public void setTopologyService(ZoneTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Property(required = false)
    public void setEnableHttps(boolean enableHttps) {
        this.enableHttps = enableHttps;
    }

    @Property(required = false)
    public void setHttpPort(String httpPort) {
        String[] tokens = httpPort.split("-");
        if (tokens.length == 1) {
            // port specified
            minHttpPort = parsePortNumber(tokens[0], "HTTP");

        } else if (tokens.length == 2) {
            // port range specified
            minHttpPort = parsePortNumber(tokens[0], "HTTP");
            maxHttpPort = parsePortNumber(tokens[1], "HTTP");
        } else {
            throw new IllegalArgumentException("Invalid HTTP port specified in system configuration");
        }
    }

    @Property(required = false)
    public void setHttpsPort(String httpsPort) {
        String[] tokens = httpsPort.split("-");
        if (tokens.length == 1) {
            // port specified
            minHttpsPort = parsePortNumber(tokens[0], "HTTPS");

        } else if (tokens.length == 2) {
            // port range specified
            minHttpsPort = parsePortNumber(tokens[0], "HTTPS");
            maxHttpsPort = parsePortNumber(tokens[1], "HTTPS");
        } else {
            throw new IllegalArgumentException("Invalid HTTPS port specified in system configuration");
        }
    }

    @Property(required = false)
    public void setSendServerVersion(boolean sendServerVersion) {
        this.sendServerVersion = sendServerVersion;
    }

    @Property(required = false)
    public void setDebug(boolean val) {
        debug = val;
    }

    @Init
    public void init() throws JettyInitializationException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            server = new Server();
            if (authenticationService != null) {
                // setup authentication if the authentication service is available 
                Fabric3LoginService loginService = new Fabric3LoginService(authenticationService);
                server.addBean(loginService);
            }
            initializeThreadPool();
            initializeConnector();
            initializeHandlers();
            server.setStopAtShutdown(true);
            server.setSendServerVersion(sendServerVersion);
            monitor.startHttpListener(selectedHttp);
            if (enableHttps) {
                monitor.startHttpsListener(selectedHttps);
            }
            server.start();
            if (managementService != null) {
                managementService.export("StatisticsService", "HTTP", "HTTP transport statistics", statisticsHandler);
                managementService.export("ServletsService", "HTTP", "Servlet management beans", servletHandler);
                managementService.export("SessionManager", "HTTP", "Servlet session manager", sessionManager);
            }
            registerHttpMetadata();
        } catch (Exception e) {
            throw new JettyInitializationException("Error starting Jetty service", e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Destroy
    public void destroy() throws Exception {
        synchronized (joinLock) {
            joinLock.notifyAll();
        }
        if (managementService != null) {
            managementService.remove("StatisticsService", "HTTP");
            managementService.remove("ServletsService", "HTTP");
            managementService.remove("SessionManager", "HTTP");
        }
        if (servletHandler != null && servletHandler.getServlets() != null) {
            for (ServletHolder holder : servletHandler.getServlets()) {
                if (managementService != null) {
                    managementService.remove(holder.getName(), HTTP_SERVLETS);
                }
            }
        }
        server.stop();
    }

    public String getHostType() {
        return "Jetty";
    }

    public void suspend() {
        if (httpConnector != null && httpConnector.isRunning()) {
            try {
                httpConnector.stop();
            } catch (Exception e) {
                monitor.exception("Error suspending HTTP connector", e);
            }
        }
        if (sslConnector != null && sslConnector.isRunning()) {
            try {
                sslConnector.stop();
            } catch (Exception e) {
                monitor.exception("Error suspending SSL connector", e);
            }
        }
    }

    public void resume() {
        if (httpConnector != null && httpConnector.isStopped()) {
            try {
                httpConnector.start();
            } catch (Exception e) {
                monitor.exception("Error resuming HTTP connector", e);
            }
        }
        if (sslConnector != null && sslConnector.isStopped()) {
            try {
                sslConnector.start();
            } catch (Exception e) {
                monitor.exception("Error resuming SSL connector", e);
            }
        }
    }

    public int getHttpPort() {
        return selectedHttp;
    }

    public int getHttpsPort() {
        return selectedHttps;
    }

    public boolean isHttpsEnabled() {
        return selectedHttps != -1;
    }

    public synchronized void registerMapping(String path, Servlet servlet) {
        ServletHolder holder = new ManagedServletHolder(servlet);
        servletHandler.addServlet(holder);
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName(holder.getName());
        mapping.setPathSpec(path);
        servletHandler.addServletMapping(mapping);
        contextHandler.addServlet(holder, path);
        if (managementService != null) {
            try {
                managementService.export(holder.getName(), HTTP_SERVLETS, "Registered transport servlets", holder);
            } catch (ManagementException e) {
                monitor.exception("Exception exporting servlet management object:" + holder.getContextPath(), e);
            }
        }
    }

    public synchronized Servlet unregisterMapping(String path) {
        List<ServletMapping> mappings = new ArrayList<ServletMapping>();
        List<String> names = new ArrayList<String>();
        for (ServletMapping mapping : servletHandler.getServletMappings()) {
            for (String spec : mapping.getPathSpecs()) {
                if (spec.equals(path)) {
                    // ok even though a servlet can be mapped to multiple paths in Jetty since ServletHost only allows one path per registration
                    names.add(mapping.getServletName());
                    continue;
                }
                mappings.add(mapping);
            }
        }
        Servlet servlet = null;
        List<ServletHolder> holders = new ArrayList<ServletHolder>();
        for (ServletHolder holder : servletHandler.getServlets()) {
            if (!names.contains(holder.getName())) {
                holders.add(holder);
            } else {
                try {
                    servlet = holder.getServlet();
                    if (managementService != null) {
                        managementService.remove(holder.getName(), HTTP_SERVLETS);
                    }
                } catch (ServletException e) {
                    monitor.exception("Exception getting servlet:" + holder.getContextPath(), e);
                } catch (ManagementException e) {
                    monitor.exception("Exception removing servlet management object:" + holder.getContextPath(), e);
                }
            }
        }
        servletHandler.setServlets(holders.toArray(new ServletHolder[holders.size()]));
        servletHandler.setServletMappings(mappings.toArray(new ServletMapping[mappings.size()]));
        return servlet;
    }

    public synchronized boolean isMappingRegistered(String path) {
        ServletMapping[] mappings = servletHandler.getServletMappings();
        if (mappings == null) {
            return false;
        }
        for (ServletMapping mapping : mappings) {
            for (String spec : mapping.getPathSpecs()) {
                if (spec.equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Server getServer() {
        return server;
    }

    public void registerHandler(Handler handler) {
        rootHandler.addHandler(handler);
    }

    private void initializeConnector() throws IOException, JettyInitializationException {
        selectHttpPort();
        selectHttpsPort();

        if (enableHttps) {
            if (keyStoreManager == null) {
                throw new JettyInitializationException("Key store manager not found - a security extension must be installed");
            }
            // setup HTTP and HTTPS
            String keystore = keyStoreManager.getKeyStoreLocation().getAbsolutePath();
            String keyPassword = keyStoreManager.getKeyStorePassword();
            String trustStore = keyStoreManager.getTrustStoreLocation().getAbsolutePath();
            String trustPassword = keyStoreManager.getTrustStorePassword();
            String certPassword = keyStoreManager.getCertPassword();
            httpConnector = new ContextAwareConnector();
            httpConnector.setPort(selectedHttp);
            sslConnector = new ContextAwareSslConnector();
            sslConnector.setAllowRenegotiate(true);
            sslConnector.setPort(selectedHttps);
            sslConnector.setKeystore(keystore);
            sslConnector.setKeyPassword(keyPassword);
            sslConnector.setPassword(certPassword);
            sslConnector.setTruststore(trustStore);
            sslConnector.setTrustPassword(trustPassword);
            server.setConnectors(new Connector[]{httpConnector, sslConnector});
        } else {
            // setup HTTP
            httpConnector = new ContextAwareConnector();
            httpConnector.setPort(selectedHttp);
            httpConnector.setSoLingerTime(-1);
            server.setConnectors(new Connector[]{httpConnector});
        }
    }

    /**
     * Registers HTTP and HTTPS metadata with the topology service if it is available.
     *
     * @throws UnknownHostException if there is an error retrieving the host address
     */
    private void registerHttpMetadata() throws UnknownHostException {
        if (topologyService != null) {
            topologyService.registerMetadata(FederationConstants.HTTP_PORT_METADATA, selectedHttp);
            String host = httpConnector.getHost();
            if (host == null) {
                host = InetAddress.getLocalHost().getHostAddress();
            }
            topologyService.registerMetadata(FederationConstants.HTTP_HOST_METADATA, host);
            if (isHttpsEnabled()) {
                topologyService.registerMetadata(FederationConstants.HTTPS_PORT_METADATA, selectedHttps);
            }
        }
    }

    private void selectHttpPort() throws IOException, JettyInitializationException {
        if (maxHttpPort == -1) {
            selectedHttp = minHttpPort;
            return;
        }
        // A bit of a hack to select the port. Normally, we should select the Jetty Connector and look for a bind exception. However, Jetty does not
        // attempt to bind to the port until the server is started. Creating a disposable socket avoids having to start the Jetty server to determine
        // if the address is taken
        selectedHttp = minHttpPort;
        while (selectedHttp <= maxHttpPort) {
            try {
                ServerSocket socket = new ServerSocket(selectedHttp);
                socket.close();
                return;
            } catch (BindException e) {
                selectedHttp++;
            }
        }
        selectedHttp = -1;
        throw new JettyInitializationException(
                "Unable to find an available HTTP port. Check to ensure the system configuration specifies an open HTTP port or port range.");
    }

    private void selectHttpsPort() throws IOException, JettyInitializationException {
        if (!enableHttps) {
            return;
        }
        if (maxHttpsPort == -1) {
            selectedHttps = minHttpsPort;
            return;
        }
        selectedHttps = minHttpsPort;
        while (selectedHttps <= maxHttpsPort) {
            try {
                ServerSocket socket = new ServerSocket(selectedHttps);
                socket.close();
                return;
            } catch (BindException e) {
                selectedHttps++;
            }
        }
        selectedHttps = -1;
        throw new JettyInitializationException(
                "Unable to find an available HTTPS port. Check to ensure the system configuration specifies an open HTTPS port or port range.");
    }

    private void initializeThreadPool() {
        if (executorService == null) {
            ExecutorThreadPool threadPool = new ExecutorThreadPool(100);
            server.setThreadPool(threadPool);
        } else {
            server.setThreadPool(new Fabric3ThreadPool());
        }
    }

    private void initializeHandlers() {
        // setup the root context handler which dispatches to other contexts based on the servlet path
        statisticsHandler = new ManagedStatisticsHandler();
        server.setHandler(statisticsHandler);
        rootHandler = new ContextHandlerCollection();
        statisticsHandler.setHandler(rootHandler);
        contextHandler = new ServletContextHandler(rootHandler, ROOT);
        sessionManager = new ManagedHashSessionManager();
        HashSessionIdManager sessionIdManager = new HashSessionIdManager();
        sessionIdManager.setWorkerName(hostInfo.getRuntimeName());
        server.setSessionIdManager(sessionIdManager);
        sessionManager.setIdManager(sessionIdManager);
        SessionHandler sessionHandler = new SessionHandler(sessionManager);
        servletHandler = new ManagedServletHandler();
        sessionHandler.setHandler(servletHandler);
        contextHandler.setHandler(sessionHandler);

        try {
            statisticsHandler.start();
            statisticsHandler.startStatisticsCollection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int parsePortNumber(String portVal, String portType) {
        int port;
        try {
            port = Integer.parseInt(portVal);
            if (port < 0) {
                throw new IllegalArgumentException("Invalid " + portType + " port number specified in the system configuration" + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + portType + " port number specified in the system configuration", e);
        }
        return port;
    }

    /**
     * An integration wrapper to enable use of a {@link ExecutorService} with Jetty
     */
    private class Fabric3ThreadPool implements ThreadPool {

        public boolean dispatch(Runnable work) {
            executorService.execute(work);
            return true;
        }

        public void join() throws InterruptedException {
            synchronized (joinLock) {
                joinLock.wait();
            }
        }

        public int getThreads() {
            throw new UnsupportedOperationException();
        }

        public int getIdleThreads() {
            throw new UnsupportedOperationException();
        }

        public boolean isLowOnThreads() {
            // TODO FIXME
            return false;
        }

    }

}
