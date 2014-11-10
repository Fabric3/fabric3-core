/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.binding.ws.model;

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
