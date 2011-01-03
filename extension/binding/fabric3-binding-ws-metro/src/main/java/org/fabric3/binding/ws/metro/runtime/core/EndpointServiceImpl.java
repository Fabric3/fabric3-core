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
package org.fabric3.binding.ws.metro.runtime.core;

import java.util.concurrent.ExecutorService;

import com.sun.xml.wss.SecurityEnvironment;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.ws.metro.MetroBindingMonitor;
import org.fabric3.spi.host.ServletHost;

/**
 * @version $Rev$ $Date$
 */
public class EndpointServiceImpl implements EndpointService {
    private SecurityEnvironment securityEnvironment;
    private ExecutorService executorService;
    private ServletHost servletHost;
    private MetroBindingMonitor monitor;

    private MetroServlet metroServlet;

    public EndpointServiceImpl(@Reference SecurityEnvironment securityEnvironment,
                               @Reference ExecutorService executorService,
                               @Reference ServletHost servletHost,
                               @Monitor MetroBindingMonitor monitor) {
        this.securityEnvironment = securityEnvironment;
        this.executorService = executorService;
        this.monitor = monitor;
        this.servletHost = servletHost;
    }

    @Init
    public void init() {
        metroServlet = new MetroServlet(executorService, securityEnvironment);
    }

    public void registerService(EndpointConfiguration configuration) throws EndpointException {
        String servicePath = configuration.getServicePath();
        if (servletHost.isMappingRegistered(servicePath)) {
            // wire reprovisioned
            unregisterService(servicePath);
        }
        servletHost.registerMapping(servicePath, metroServlet);
        // register <endpoint-url/mex> address for serving WS-MEX requests
        servletHost.registerMapping(servicePath + "/mex", metroServlet);
        metroServlet.registerService(configuration);
        monitor.endpointProvisioned(servicePath);
    }

    public void unregisterService(String path) {
        servletHost.unregisterMapping(path);
        servletHost.unregisterMapping(path + "/mex");
        metroServlet.unregisterService(path);
        monitor.endpointRemoved(path);
    }
}
