/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.runtime.standalone.server;

import java.io.IOException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;

/**
 * Shuts down a server instance via JMX. Valid parameters are <code>-address (-a)</code> and <code>-port (-p)</code> if connecting to a server not
 * using the default IP address (localhost) and/or JMX port (1199).
 */
public class Shutdown {
    private static final String RUNTIME_MBEAN = "fabric3:SubDomain=runtime, type=component, name=RuntimeMBean";
    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_ADMIN_PORT = 1199;


    public static void main(String[] args) {
        if (args.length != 0 && args.length != 2 && args.length != 4) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        Shutdown shutdown = new Shutdown();
        Parameters parameters = shutdown.parse(args);
        if (parameters.isError()) {
            System.out.println("ERROR: " + parameters.getErrorMessage());
        }
        try {
            shutdown.shutdown(parameters);
        } catch (JMException | IOException e) {
            System.out.println("ERROR: Unable to shutdown remote server");
            e.printStackTrace();
        }

    }

    private void shutdown(Parameters parameters) throws JMException, IOException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + parameters.getAddress() + ":" + parameters.getPort() + "/server");
        RMIConnector rmiConnector = new RMIConnector(url, null);
        rmiConnector.connect();

        MBeanServerConnection connection = rmiConnector.getMBeanServerConnection();
        ObjectName name = new ObjectName(RUNTIME_MBEAN);
        connection.invoke(name, "shutdownRuntime", null, null);
        System.out.println("Fabric3 shutdown");
    }


    private Parameters parse(String[] args) {
        int port = DEFAULT_ADMIN_PORT;
        String address = DEFAULT_ADDRESS;
        if (args.length == 0) {
            return new Parameters(DEFAULT_ADDRESS, DEFAULT_ADMIN_PORT);
        } else if (args.length == 2) {
            if (isAddress(args[0])) {
                address = args[1];
            } else if (isPort(args[0])) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new Parameters("Invalid port: " + args[1]);
                }
            } else {
                return new Parameters("Invalid parameter type: " + args[0]);
            }
        } else if (args.length == 4) {
            if (isAddress(args[0])) {
                address = args[1];
            } else if (isPort(args[0])) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return new Parameters("Invalid port: " + args[1]);
                }
            } else {
                return new Parameters("Invalid parameter type: " + args[0]);
            }
            if (isAddress(args[2])) {
                address = args[3];
            } else if (isPort(args[2])) {
                try {
                    port = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    return new Parameters("Invalid port: " + args[3]);
                }
            } else {
                return new Parameters("Invalid parameter type: " + args[3]);
            }

        }
        return new Parameters(address, port);
    }

    private boolean isAddress(String arg) {
        return "-address".equals(arg) || "-a".equals(arg);
    }

    private boolean isPort(String arg) {
        return "-port".equals(arg) || "-p".equals(arg);
    }

    private class Parameters {
        private String address;
        private int port;
        private boolean error;
        private String errorMessage;

        public Parameters(String address, int port) {
            this.port = port;
            this.address = address;
        }

        private Parameters(String errorMessage) {
            this.errorMessage = errorMessage;
            error = true;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public boolean isError() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
