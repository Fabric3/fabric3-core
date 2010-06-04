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
package org.fabric3.transport.jetty.impl;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.security.KeyStoreManager;
import org.fabric3.transport.jetty.JettyService;

/**
 * Implements an HTTP transport service using Jetty.
 *
 * @version $$Rev$$ $$Date$$
 */
@EagerInit
public class JettyServiceImpl implements JettyService {

    private static final String ROOT = "/";

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
    private KeyStoreManager keyStoreManager;
    private TransportMonitor monitor;
    private WorkScheduler scheduler;
    private boolean debug;
    private Server server;
    private ServletHandler servletHandler;
    private ContextHandlerCollection rootHandler;

    static {
        // hack to replace the static Jetty logger
        System.setProperty("org.mortbay.log.class", JettyLogger.class.getName());
    }

    @Constructor
    public JettyServiceImpl(@Reference WorkScheduler scheduler, @Monitor TransportMonitor monitor) {
        this.scheduler = scheduler;
        this.monitor = monitor;
        // Jetty uses a static logger, so jam in the monitor into a static reference
        Logger logger = Log.getLogger(null);
        if (logger instanceof JettyLogger) {
            JettyLogger jettyLogger = (JettyLogger) logger;
            jettyLogger.setMonitor(this.monitor);
            if (debug) {
                jettyLogger.setDebugEnabled(true);
            }
        }
    }

    public JettyServiceImpl(TransportMonitor monitor) {
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setKeyStoreManager(KeyStoreManager keyStoreManager) {
        this.keyStoreManager = keyStoreManager;
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
        try {
            server = new Server();
            initializeThreadPool();
            initializeConnector();
            initializeRootContextHandler();
            server.setStopAtShutdown(true);
            server.setSendServerVersion(sendServerVersion);
            monitor.startHttpListener(selectedHttp);
            if (enableHttps) {
                monitor.startHttpsListener(selectedHttps);
            }
            server.start();
        } catch (Exception e) {
            throw new JettyInitializationException("Error starting Jetty service", e);
        }
    }

    @Destroy
    public void destroy() throws Exception {
        synchronized (joinLock) {
            joinLock.notifyAll();
        }
        server.stop();
    }

    public int getHttpPort() {
        return selectedHttp;
    }

    public int getHttpsPort() {
        return selectedHttps;
    }

    public ServletContext getServletContext() {
        return servletHandler.getServletContext();
    }

    public boolean isHttpsEnabled() {
        return selectedHttps != -1;
    }

    public synchronized void registerMapping(String path, Servlet servlet) {
        ServletHolder holder = new ServletHolder(servlet);
        servletHandler.addServlet(holder);
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName(holder.getName());
        mapping.setPathSpec(path);
        servletHandler.addServletMapping(mapping);
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
                } catch (ServletException e) {
                    e.printStackTrace();
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
            if (keyStoreManager == null){
                throw new JettyInitializationException("Key store manager not found - a security extension must be installed");
            }
            // setup HTTP and HTTPS
            String keystore = keyStoreManager.getKeyStoreLocation().getAbsolutePath();
            String keyPassword = keyStoreManager.getKeyStorePassword();
            String truststore = keyStoreManager.getTrustStoreLocation().getAbsolutePath();
            String trustPassword = keyStoreManager.getTrustStorePassword();
            String certPassword = keyStoreManager.getCertPassword();
            Connector httpConnector = new SelectChannelConnector();
            httpConnector.setPort(selectedHttp);
            SslSocketConnector sslConnector = new SslSocketConnector();
            sslConnector.setPort(selectedHttps);
            sslConnector.setKeystore(keystore);
            sslConnector.setKeyPassword(keyPassword);
            sslConnector.setPassword(certPassword);
            sslConnector.setTruststore(truststore);
            sslConnector.setTrustPassword(trustPassword);
            server.setConnectors(new Connector[]{httpConnector, sslConnector});
        } else {
            // setup HTTP
            SelectChannelConnector selectConnector = new SelectChannelConnector();
            selectConnector.setPort(selectedHttp);
            selectConnector.setSoLingerTime(-1);
            server.setConnectors(new Connector[]{selectConnector});
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
        if (scheduler == null) {
            BoundedThreadPool threadPool = new BoundedThreadPool();
            threadPool.setMaxThreads(100);
            server.setThreadPool(threadPool);
        } else {
            server.setThreadPool(new Fabric3ThreadPool());
        }
    }

    private void initializeRootContextHandler() {
        // setup the root context handler which dispatches to other contexts based on the servlet path
        rootHandler = new ContextHandlerCollection();
        server.setHandler(rootHandler);
        ContextHandler contextHandler = new ContextHandler(rootHandler, ROOT);
        SessionHandler sessionHandler = new SessionHandler();
        servletHandler = new ServletHandler();
        sessionHandler.addHandler(servletHandler);
        contextHandler.addHandler(sessionHandler);
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
     * An integration wrapper to enable use of a {@link WorkScheduler} with Jetty
     */
    private class Fabric3ThreadPool implements ThreadPool {

        public boolean dispatch(Runnable job) {
            scheduler.scheduleWork(new Fabric3Work(job));
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

    /**
     * A unit of work dispatched to the runtime work scheduler
     */
    private class Fabric3Work extends DefaultPausableWork {

        Runnable job;

        public Fabric3Work(Runnable job) {
            this.job = job;
        }

        public void execute() {
            job.run();
        }
    }

}
