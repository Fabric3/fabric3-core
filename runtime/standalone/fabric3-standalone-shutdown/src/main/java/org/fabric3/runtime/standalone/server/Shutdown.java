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
        } catch (JMException e) {
            System.out.println("ERROR: Unable to shutdown remote server");
            e.printStackTrace();
        } catch (IOException e) {
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
