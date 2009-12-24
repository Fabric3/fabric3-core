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
package org.fabric3.binding.ws.metro.generator.resolver;

import java.net.MalformedURLException;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.EffectivePolicy;
import org.fabric3.spi.topology.DomainManager;

/**
 * Default implementation of TargetUrlResolver.
 *
 * @version $Rev$ $Date$
 */
public class TargetUrlResolverImpl implements TargetUrlResolver {
    private DomainManager domainManager;
    private ServletHost servletHost;

    /**
     * Constructor.
     *
     * @param servletHost   the servlet host, used for determining the endpoint port  in a single-VM environment
     * @param domainManager the optional domain manager, used for determining the endpoint address and port in a multi-VM environment. The reference
     *                      is optional since a domain manager will not be present in a single-VM environment.
     */
    public TargetUrlResolverImpl(@Reference ServletHost servletHost, @Reference(required = false) DomainManager domainManager) {
        this.servletHost = servletHost;
        this.domainManager = domainManager;
    }

    public URL resolveUrl(LogicalBinding<WsBindingDefinition> serviceBinding, EffectivePolicy policy) throws GenerationException {
        try {
            URL targetUrl;
            String path = serviceBinding.getDefinition().getTargetUri().toString();
            if (path == null) {
                path = serviceBinding.getParent().getUri().getFragment();
            }
            boolean https = requiresHttps(policy);
            if (domainManager != null) {
                // distributed domain, get the remote node HTTP/S information
                String zone = serviceBinding.getParent().getParent().getZone();
                if (https) {
                    String base = domainManager.getTransportMetaData(zone, String.class, "https");
                    targetUrl = new URL("https://" + base + "/" + path);
                } else {
                    String base = domainManager.getTransportMetaData(zone, String.class, "http");
                    targetUrl = new URL("http://" + base + "/" + path);
                }
            } else {
                // single VM
                if (https) {
                    targetUrl = new URL("https://localhost:" + servletHost.getHttpsPort() + "/" + path);
                } else {
                    targetUrl = new URL("http://localhost:" + servletHost.getHttpPort() + "/" + path);
                }

            }
            return targetUrl;
        } catch (MalformedURLException e) {
            throw new GenerationException(e);
        }

    }


    private boolean requiresHttps(EffectivePolicy policy) {
        for (Intent intent : policy.getEndpointIntents()) {
            String localPart = intent.getName().getLocalPart();
            if (localPart.startsWith("authorization") || localPart.equals("integrity") || localPart.startsWith("confidentiality")
                    || localPart.startsWith("mutualAuthentication") || localPart.equals("authentication") || localPart.startsWith("clientAuthentication")
                    || localPart.startsWith("serverAuthentication")) {
                return true;
            }
        }
        return false;
    }

}
