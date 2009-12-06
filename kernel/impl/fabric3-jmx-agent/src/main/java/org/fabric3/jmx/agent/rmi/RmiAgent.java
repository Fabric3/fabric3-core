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
package org.fabric3.jmx.agent.rmi;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import javax.management.remote.JMXServiceURL;

import org.fabric3.jmx.agent.AbstractAgent;
import org.fabric3.jmx.agent.ManagementException;

/**
 * Utility for starting the JMX server with an RMI agent.
 *
 * @version $Revsion$ $Date$
 */
public class RmiAgent extends AbstractAgent {

    private Registry registry;
    private int assignedPort;

    public RmiAgent(int port) throws ManagementException {
        super(port, -1);
    }

    public RmiAgent(int minPort, int maxPort) throws ManagementException {
        super(minPort, maxPort);
    }

    @Override
    public void preStart() throws ManagementException {
        int port = getMinPort();
        if (getMaxPort() == -1) {
            try {
                registry = LocateRegistry.createRegistry(minPort);
                assignedPort = minPort;
            } catch (RemoteException ex) {
                throw new ManagementException(ex);
            }
        } else {
            assignedPort = minPort;
            while (port <= getMaxPort()) {
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

    public int getAssignedPort() {
        return assignedPort;
    }

    @Override
    public void postStop() throws ManagementException {

        try {
            if (registry != null) {
                UnicastRemoteObject.unexportObject(registry, true);
            }
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    protected JMXServiceURL getAdaptorUrl() throws ManagementException {

        try {
            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + assignedPort + "/server");
            // service:jmx:rmi:///jndi/rmi://localhost:1199/server
        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        }

    }


}
