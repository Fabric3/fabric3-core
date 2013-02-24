/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.ws.metro.provision;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * Encapsulates endpoint information for the reference side of an invocation chain.
 */
public class ReferenceEndpointDefinition extends AbstractEndpointDefinition {
    private static final long serialVersionUID = -7422624061436929193L;

    private boolean defaultServiceName;
    private QName portTypeName;
    private URL url;

    /**
     * Constructor.
     *
     * @param serviceName        the qualified name of the target service
     * @param defaultServiceName true if the service name is a default and may be overriden by introspecting WSDL
     * @param portName           the port name
     * @param portTypeName       the qualified name of the target port type
     * @param url                the endpoint URL
     */
    public ReferenceEndpointDefinition(QName serviceName, boolean defaultServiceName, QName portName, QName portTypeName, URL url) {
        super(serviceName, portName, null);
        this.defaultServiceName = defaultServiceName;
        this.portTypeName = portTypeName;
        this.url = url;
    }

    public ReferenceEndpointDefinition(QName serviceName, boolean defaultServiceName, QName portName, QName portTypeName, URL url, String wsdl) {
        super(serviceName, portName, wsdl);
        this.defaultServiceName = defaultServiceName;
        this.portTypeName = portTypeName;
        this.url = url;
    }

    /**
     * Returns true if the service name is a default and may be overridden by introspecting WSDL. If a service name is not specified using the
     * <code>WebService.serviceName</code> annotation attribute, it is calculated according to JAX-WS mapping rules. However, web services stacks such as WCF
     * (.NET) adopt different defaulting schemes. To accommodate this, during reference proxy creation, the Fabric3 runtime will introspect the target WSDL to
     * determine the actual service name if the default name is not valid. The actual name will be determined by mapping the portType name to a service defined
     * in the WSDL. Note this can only be done if the WSDL contains exactly one service that uses the portType. Otherwise, a serviceName will need to be
     * explicitly specified using the <code>WebService</code> annotation or wsdlElement attribute of binding.ws.
     *
     * @return true if the service name is a default and may be overriden by introspecting WSDL
     */
    public boolean isDefaultServiceName() {
        return defaultServiceName;
    }

    /**
     * Returns the qualified port type name.
     *
     * @return the qualified port type name
     */
    public QName getPortTypeName() {
        return portTypeName;
    }

    /**
     * Returns the endpoint URL.
     *
     * @return the endpoint URL
     */
    public URL getUrl() {
        return url;
    }

}
