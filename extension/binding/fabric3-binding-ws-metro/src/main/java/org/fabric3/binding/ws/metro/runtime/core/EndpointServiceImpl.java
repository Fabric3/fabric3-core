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
package org.fabric3.binding.ws.metro.runtime.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import com.sun.xml.wss.SecurityEnvironment;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ws.metro.MetroBindingMonitor;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.host.ServletHost;

/**
 * @version $Rev$ $Date$
 */
public class EndpointServiceImpl implements EndpointService {

    private SecurityEnvironment securityEnvironment;
    private WorkScheduler scheduler;
    private ServletHost servletHost;
    private MetroBindingMonitor monitor;

    // cached registered port names used to avoid duplicate registrations since they are not supported by Metro
    private Set<QName> registeredPorts = new HashSet<QName>();
    private Map<String, QName> pathToPorts = new HashMap<String, QName>();

    private MetroServlet metroServlet;

    public EndpointServiceImpl(@Reference SecurityEnvironment securityEnvironment,
                               @Reference WorkScheduler scheduler,
                               @Reference ServletHost servletHost,
                               @Monitor MetroBindingMonitor monitor) {
        this.securityEnvironment = securityEnvironment;
        this.scheduler = scheduler;
        this.monitor = monitor;
        this.servletHost = servletHost;
    }

    @Init
    public void init() {
        metroServlet = new MetroServlet(scheduler, securityEnvironment);
    }

    public void registerService(EndpointConfiguration configuration) throws EndpointException {
        QName portName = configuration.getPortName();
        if (registeredPorts.contains(portName)) {
            throw new EndpointException("Port already registered: " + portName);
        }
        String servicePath = configuration.getServicePath();
        registeredPorts.add(portName);
        pathToPorts.put(servicePath, portName);
        servletHost.registerMapping(servicePath, metroServlet);
        // register <endpoint-url/mex> address for serving WS-MEX requests
        servletHost.registerMapping(servicePath + "/mex", metroServlet);
        metroServlet.registerService(configuration);
        monitor.endpointProvisioned(servicePath);
    }

    public void unregisterService(String path) {
        metroServlet.unregisterService(path);
        QName portName = pathToPorts.remove(path);
        registeredPorts.remove(portName);
        monitor.endpointRemoved(path);
    }
}
