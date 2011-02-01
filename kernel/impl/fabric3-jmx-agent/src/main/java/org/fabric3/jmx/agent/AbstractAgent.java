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
*/
package org.fabric3.jmx.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Default agent.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractAgent implements Agent {
    private static final String DOMAIN = "fabric3";

    private JMXAuthenticator authenticator;
    protected int minPort;
    private int maxPort;

    private MBeanServer mBeanServer;
    private AtomicBoolean started = new AtomicBoolean();
    private JMXConnectorServer connectorServer;

    /**
     * Instantiates an agent that does not enable JMX security.
     *
     * @param minPort the minimum port number
     * @param maxPort the maximum port number
     */
    protected AbstractAgent(int minPort, int maxPort) {
        this(null, minPort, maxPort);
    }

    /**
     * Instantiates an agent with JMX security enabled.
     *
     * @param authenticator the authenticator
     * @param minPort       the minimum port number
     * @param maxPort       the maximum port number
     */
    public AbstractAgent(JMXAuthenticator authenticator, int minPort, int maxPort) {
        this.authenticator = authenticator;
        this.minPort = minPort;
        this.maxPort = maxPort;
        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    public void start() throws ManagementException {
        try {
            if (started.get()) {
                throw new IllegalArgumentException("Agent already started");
            }
            preStart();
            JMXServiceURL url = getAdaptorUrl();
            Map<String, Object> environment = new HashMap<String, Object>();
            if (authenticator != null) {
                environment.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
            }
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mBeanServer);
            connectorServer.start();
            started.set(true);
        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }
    }

    public void stop() throws ManagementException {
        try {
            if (!started.get()) {
                throw new IllegalArgumentException("Agent not started");
            }
            connectorServer.stop();
            postStop();
            started.set(false);
            synchronized (this) {
                notify();
            }
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }
    }

    public int getMinPort() {
        return minPort;
    }

    public int getMaxPort() {
        return maxPort;
    }

    /**
     * Gets the adaptor URL.
     *
     * @return Adaptor URL.
     * @throws ManagementException if there is an error returning the URL
     */
    protected abstract JMXServiceURL getAdaptorUrl() throws ManagementException;

    /**
     * Performs initialization for a protocol-specific agent.
     *
     * @throws ManagementException if an initialization error occurs.
     */
    protected abstract void preStart() throws ManagementException;

    /**
     * Performs a shutdown operation for protocol-specific agent.
     *
     * @throws ManagementException if an shutdown error occurs.
     */
    protected abstract void postStop() throws ManagementException;


}