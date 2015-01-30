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

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.ParseException;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class RmiAgent {
    private static final int DEFAULT_JMX_PORT = 1199;
    private int port = -1;
    private boolean disabled;
    private JmxSecurity security = JmxSecurity.DISABLED;

    private Registry registry;
    private Port assignedPort;
    private MBeanServer mBeanServer;
    private PortAllocator portAllocator;
    private RmiAgentMonitor monitor;

    private DelegatingJmxAuthenticator authenticator;
    private JMXConnectorServer connectorServer;

    public RmiAgent(@Reference MBeanServer mBeanServer,
                    @Reference DelegatingJmxAuthenticator authenticator,
                    @Reference PortAllocator portAllocator,
                    @Monitor RmiAgentMonitor monitor) {
        this.mBeanServer = mBeanServer;
        this.authenticator = authenticator;
        this.portAllocator = portAllocator;
        this.monitor = monitor;
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
                port = parsePortNumber(ports);

            } else if (tokens.length == 2) {
                throw new IllegalArgumentException("Port ranges no longer supported via JMX configuration. Use the runtime port.range attribute");
            }
        }
    }

    @Property(required = false)
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Init
    public void init() throws ContainerException {
        if (disabled) {
            return;
        }
        try {
            createRegistry();
            Map<String, Object> environment = initEnvironment();
            initConnector(environment);
            monitor.jmxStarted(assignedPort.getNumber());
        } catch (IOException ex) {
            throw new ContainerException(ex);
        }
    }

    @Destroy
    public void destroy() throws ContainerException {
        if (disabled) {
            return;
        }
        try {
            connectorServer.stop();
            removeRegistry();
            portAllocator.release("JMX");
            synchronized (this) {
                notify();
            }
        } catch (IOException ex) {
            throw new ContainerException(ex);
        }
    }

    private Map<String, Object> initEnvironment() {
        Map<String, Object> environment = new HashMap<>();
        if (JmxSecurity.DISABLED != security) {
            environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        }
        return environment;
    }

    private void initConnector(Map<String, Object> environment) throws IOException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + assignedPort.getNumber() + "/server");
        // service:jmx:rmi:///jndi/rmi://localhost:1199/server
        connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mBeanServer);
        connectorServer.start();
    }

    private void createRegistry() throws ContainerException {
        if (port == -1) {
            // port not assigned, get one from the allocator
            try {
                if (portAllocator.isPoolEnabled()) {
                    assignedPort = portAllocator.allocate("JMX", "JMX");
                } else {
                    assignedPort = portAllocator.reserve("JMX", "JMX", DEFAULT_JMX_PORT);
                }
                assignedPort.bind(Port.TYPE.TCP);
                registry = LocateRegistry.createRegistry(assignedPort.getNumber());
            } catch (RemoteException e) {
                throw new ContainerException(e);
            }
        } else {
            // port is explicitly assigned
            try {
                assignedPort = portAllocator.reserve("JMX", "JMX", port);
                assignedPort.bind(Port.TYPE.TCP);
                registry = LocateRegistry.createRegistry(assignedPort.getNumber());
            } catch (RemoteException e) {
                throw new ContainerException(e);
            }
        }
    }

    private void removeRegistry() throws ContainerException {
        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
        } catch (IOException ex) {
            throw new ContainerException(ex);
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
