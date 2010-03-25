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
package org.fabric3.binding.net.generator;

import java.net.MalformedURLException;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.model.TcpBindingDefinition;
import org.fabric3.binding.net.runtime.TransportService;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * Default implementation of NetTargetUrlResolver.
 *
 * @version $Rev$ $Date$
 */
public class NetTargetUrlResolverImpl implements NetTargetUrlResolver {
    private TransportService transportService;
    private DomainTopologyService topologyService;

    /**
     * Constructor.
     *
     * @param transportService the optional {@link TransportService}, used for determining the endpoint address and port in a single-VM environment.
     *                         The reference is optional since a TransportService will not be present on the controller in a multi-VM environment.
     * @param topologyService  the optional {@link DomainTopologyService}, used for determining the endpoint address and port in a multi-VM
     *                         environment. The reference is optional since a DomainTopologyService will not be present in a single-VM environment.
     */
    public NetTargetUrlResolverImpl(@Reference(required = false) TransportService transportService,
                                    @Reference(required = false) DomainTopologyService topologyService) {
        this.transportService = transportService;
        this.topologyService = topologyService;
    }

    public URL resolveUrl(LogicalBinding<?> serviceBinding) throws GenerationException {
        try {
            URL targetUrl;
            BindingDefinition definition = serviceBinding.getDefinition();
            String path = definition.getTargetUri().toString();
            if (path == null) {
                path = serviceBinding.getParent().getUri().getFragment();
            }
            if (topologyService != null) {
                // distributed domain, get the remote node HTTP/S information
                String zone = serviceBinding.getParent().getParent().getZone();
                String base;
                if (definition instanceof TcpBindingDefinition) {
                    base = topologyService.getTransportMetaData(zone, "binding.net.tcp");
                } else {
                    base = topologyService.getTransportMetaData(zone, "binding.net.http");
                }
                targetUrl = new URL("http://" + base + path);
            } else {
                // single VM
                targetUrl = new URL("http://localhost:" + transportService.getHttpPort() + path);

            }
            return targetUrl;
        } catch (MalformedURLException e) {
            throw new GenerationException(e);
        }

    }


}