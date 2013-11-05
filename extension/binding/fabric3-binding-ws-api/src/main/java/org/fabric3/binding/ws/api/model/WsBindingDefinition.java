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
package org.fabric3.binding.ws.api.model;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.oasisopen.sca.Constants;

/**
 * Logical binding definition for web services.
 */
public class WsBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -2097314069798596206L;
    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.ws");

    private String wsdlLocation;
    private String wsdlElement;
    private Map<String, String> configuration;
    private int retries;

    /**
     * Constructor
     *
     * @param name         the binding name. May be null
     * @param targetUri    the target binding URI. May be null
     * @param wsdlLocation the WSDL location. May be null
     * @param wsdlElement  the SCA WSDL element expression. May be null
     * @param retries      the number of retries in the event the target service is unavailable during an invocation
     */
    public WsBindingDefinition(String name, URI targetUri, String wsdlLocation, String wsdlElement, int retries) {
        super(name, targetUri, BINDING_QNAME);
        this.wsdlElement = wsdlElement;
        this.wsdlLocation = wsdlLocation;
        this.retries = retries;
        this.configuration = Collections.emptyMap();
    }

    /**
     * Constructor for callback bindings.
     */
    public WsBindingDefinition() {
        this(null, null, null, null, 0);
    }

    /**
     * Returns the SCA expression pointing to the WSDL element this endpoint should use or null.
     *
     * @return the SCA expression or null
     */
    public String getWsdlElement() {
        return wsdlElement;
    }

    /**
     * Returns the location of the endpoint WSDL or null if not specified.
     *
     * @return the location of the endpoint WSDL
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * The number of retries in the event the target service is unavailable during an invocation.
     *
     * @return the number of retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Returns optional binding configuration.
     *
     * @return optional binding configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * Sets optional configuration for the binding.
     *
     * @param configuration optional configuration for the binding
     */
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    /**
     * Overrides the target URI.
     *
     * @param targetUri the target URI
     */
    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }
}
