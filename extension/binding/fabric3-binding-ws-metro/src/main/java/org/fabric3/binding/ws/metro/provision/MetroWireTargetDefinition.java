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
 */
package org.fabric3.binding.ws.metro.provision;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Base class for reference-side wire definitions.
 */
public abstract class MetroWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 5758003268658918242L;

    private ReferenceEndpointDefinition endpointDefinition;
    private List<QName> intents;
    private SecurityConfiguration securityConfiguration;
    private ConnectionConfiguration connectionConfiguration;
    private String wsdl;
    private List<PhysicalBindingHandlerDefinition> handlers;
    private boolean bidirectional;

    /**
     * Constructor.
     *
     * @param endpointDefinition      endpoint metadata
     * @param wsdl                    the endpoint WSDL or null if the WSDL can be derived from the SEI without the need to merge policy
     * @param intents                 intents configured at the endpoint level that are provided natively by the Metro
     * @param securityConfiguration   the security configuration or null if security is not configured
     * @param connectionConfiguration the HTTP configuration or null if defaults should be used
     * @param bidirectional           true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers                optional binding handlers
     */
    public MetroWireTargetDefinition(ReferenceEndpointDefinition endpointDefinition,
                                     String wsdl,
                                     List<QName> intents,
                                     SecurityConfiguration securityConfiguration,
                                     ConnectionConfiguration connectionConfiguration,
                                     boolean bidirectional,
                                     List<PhysicalBindingHandlerDefinition> handlers) {
        this.endpointDefinition = endpointDefinition;
        this.wsdl = wsdl;
        this.intents = intents;
        this.securityConfiguration = securityConfiguration;
        this.connectionConfiguration = connectionConfiguration;
        this.bidirectional = bidirectional;
        this.handlers = handlers;
    }

    /**
     * Returns the endpoint information.
     *
     * @return the endpoint information
     */
    public ReferenceEndpointDefinition getEndpointDefinition() {
        return endpointDefinition;
    }

    /**
     * Returns the serialized WSDL for the target service.
     *
     * @return the serialized WSDL for the target service
     */
    public String getWsdl() {
        return wsdl;
    }

    /**
     * Returns the configured endpoint intents provided by the Metro.
     *
     * @return the intents
     */
    public List<QName> getIntents() {
        return intents;
    }

    /**
     * Returns the security configuration.
     *
     * @return the security configuration
     */
    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    /**
     * Returns the HTTP connection configuration.
     *
     * @return the HTTP connection configuration
     */
    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * True if this wire is bidirectional.
     *
     * @return true if this wire is bidirectional
     */
    public boolean isBidirectional() {
        return bidirectional;
    }

    /**
     * Returns the optional handlers to engage when processing an invocation.
     *
     * @return the optional handlers to engage when processing an invocation.
     */
    public List<PhysicalBindingHandlerDefinition> getHandlers() {
        return handlers;
    }
}
