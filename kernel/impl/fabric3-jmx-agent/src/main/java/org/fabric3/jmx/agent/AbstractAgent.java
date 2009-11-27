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
*/
package org.fabric3.jmx.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Default agent.
 *
 * @version $Revison$ $Date$
 */
public abstract class AbstractAgent implements Agent {

    private static final String DOMAIN = "fabric3";
    private MBeanServer mBeanServer;
    private AtomicBoolean started = new AtomicBoolean();
    private JMXConnectorServer connectorServer;
    protected int minPort;
    private int maxPort;

    /**
     * Constructor using the default RMI port (1099).
     *
     * @throws ManagementException If unable to start the agent.
     */
    public AbstractAgent() throws ManagementException {
        this(1099, -1);
    }

    /**
     * Constructor using the given port range.
     *
     * @param minPort the minimum port number
     * @param maxPort the maximum port number
     */
    protected AbstractAgent(int minPort, int maxPort) {
        this.minPort = minPort;
        this.maxPort = maxPort;
        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    public final void start() throws ManagementException {

        try {

            if (started.get()) {
                throw new IllegalArgumentException("Agent already started");
            }

            preStart();

            JMXServiceURL url = getAdaptorUrl();
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);

            connectorServer.start();

            started.set(true);

        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    public final void run() {
        while (started.get()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // continue;
                }
            }
        }
    }

    public final void shutdown() throws ManagementException {

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
     */
    protected abstract JMXServiceURL getAdaptorUrl() throws ManagementException;

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void preStart() throws ManagementException;

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void postStop() throws ManagementException;


}