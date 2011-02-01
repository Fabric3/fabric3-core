/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.jmx.agent;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.ParseException;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class RmiAgent {
    private int minPort = 1199;
    private int maxPort = 1199;
    private JmxSecurity security = JmxSecurity.DISABLED;

    private Registry registry;
    private int assignedPort;
    private MBeanServer mBeanServer;
    private RmiAgentMonitor monitor;

    private DelegatingJmxAuthenticator authenticator;
    private JMXConnectorServer connectorServer;

    public RmiAgent(@Reference MBeanServer mBeanServer, @Reference DelegatingJmxAuthenticator authenticator, @Monitor RmiAgentMonitor monitor) {
        this.mBeanServer = mBeanServer;
        this.monitor = monitor;
        this.authenticator = authenticator;
    }

    @Property(required = false)
    public void setSecurity(String level) throws ParseException {
        try {
            security = JmxSecurity.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid JMX security setting:" + level);
        }
    }

    @Property(required = false)
    public void setJmxPort(String ports) {
        if (ports.length() > 0) {
            String[] tokens = ports.split("-");
            if (tokens.length == 1) {
                // port specified
                minPort = parsePortNumber(ports);
                maxPort = minPort;

            } else if (tokens.length == 2) {
                // port range specified
                minPort = parsePortNumber(tokens[0]);
                maxPort = parsePortNumber(tokens[1]);
            } else {
                throw new IllegalArgumentException("Invalid JMX port specified in system configuration: " + ports);
            }
        }
    }

    @Init
    public void init() throws ManagementException {
        try {
            createRegistry();
            Map<String, Object> environment = initEnvironment();
            initConnector(environment);
            monitor.jmxStarted(assignedPort);
        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }
    }

    @Destroy
    public void destroy() throws ManagementException {
        try {
            connectorServer.stop();
            removeRegistry();
            synchronized (this) {
                notify();
            }
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }
    }

    int getMinPort() {
        return minPort;
    }

    int getMaxPort() {
        return maxPort;
    }
    
    private Map<String, Object> initEnvironment() {
        Map<String, Object> environment = new HashMap<String, Object>();
        if (JmxSecurity.DISABLED != security) {
            environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        }
        return environment;
    }

    private void initConnector(Map<String, Object> environment) throws IOException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + assignedPort + "/server");
        // service:jmx:rmi:///jndi/rmi://localhost:1199/server
        connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mBeanServer);
        connectorServer.start();
    }

    private void createRegistry() throws ManagementException {
        int port = minPort;
        if (maxPort == -1) {
            try {
                registry = LocateRegistry.createRegistry(minPort);
                assignedPort = minPort;
            } catch (RemoteException ex) {
                throw new ManagementException(ex);
            }
        } else {
            assignedPort = minPort;
            while (port <= maxPort) {
                try {
                    registry = LocateRegistry.createRegistry(assignedPort);
                    return;
                } catch (ExportException ex) {
                    if (ex.getCause() instanceof BindException) {
                        ++assignedPort;
                        continue;
                    }
                    throw new ManagementException(ex);
                } catch (RemoteException ex) {
                    throw new ManagementException(ex);
                }
            }
        }
    }

    private void removeRegistry() throws ManagementException {
        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    private int parsePortNumber(String portVal) {
        int port;
        try {
            port = Integer.parseInt(portVal);
            if (port < 0) {
                throw new IllegalArgumentException("Invalid JMX port number specified in system configuration:" + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid JMX port", e);
        }
        return port;
    }


}
