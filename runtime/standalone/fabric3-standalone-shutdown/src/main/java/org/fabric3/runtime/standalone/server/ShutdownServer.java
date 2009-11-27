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
package org.fabric3.runtime.standalone.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.management.JMException;

/**
 * @version $Rev$ $Date$
 */
public class ShutdownServer {
    private static final String MONITOR_PORT_PARAM = "fabric3.monitor.port";
    private static final String MONITOR_KEY_PARAM = "fabric3.monitor.key";
    /**
     * Fabric3 admin host.
     */
    private static final String ADMIN_HOST_PROPERTY = "fabric3.adminHost";

    /**
     * Fabric3 admin port.
     */
    private static final String ADMIN_PORT_PROPERTY = "fabric3.adminPort";

    /**
     * Default host.
     */
    private static final String DEFAULT_ADMIN_HOST = "localhost";

    /**
     * Default port.
     */
    private static final int DEFAULT_ADMIN_PORT = 1099;

    /**
     * Host.
     */
    private String host = DEFAULT_ADMIN_HOST;

    /**
     * Port.
     */
    private int port = DEFAULT_ADMIN_PORT;

    /**
     * @param args Commandline arguments.
     */
    public static void main(String[] args) throws Exception {
        String monitorKey = System.getProperty(MONITOR_KEY_PARAM, "f3");
        String portVal = System.getProperty(MONITOR_PORT_PARAM, "8083");
        int monitorPort;
        try {
            monitorPort = Integer.parseInt(portVal);
            if (monitorPort < 0) {
                throw new IllegalArgumentException("Invalid port number:" + monitorPort);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port", e);
        }

        ShutdownServer shutdownServer = new ShutdownServer();
        shutdownServer.shutdown(monitorKey, monitorPort);

    }

    /**
     * Initializes the host and the port.
     */
    private ShutdownServer() {
        host = System.getProperty(ADMIN_HOST_PROPERTY, DEFAULT_ADMIN_HOST);
        port = Integer.getInteger(ADMIN_PORT_PROPERTY, DEFAULT_ADMIN_PORT);
    }

    private void shutdown(String monitorKey, int monitorPort) throws IOException, JMException {
        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), monitorPort);
            OutputStream out = s.getOutputStream();
            out.write((monitorKey + "\r\nstop\r\n").getBytes());
            out.flush();
            s.shutdownOutput();
            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
//        RMIConnector rmiConnector = new RMIConnector(url, null);
//        rmiConnector.connect();
//
//        MBeanServerConnection con = rmiConnector.getMBeanServerConnection();
//        con.invoke(new ObjectName("fabric3:type=server,name=fabric3Server"), "shutdown", null, null);
    }
}
