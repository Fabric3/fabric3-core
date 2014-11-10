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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.rs.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 *
 */
public class RsWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = 2180952036516977449L;

    private String rsClass;
    private AuthenticationType authenticationType;

    /**
     * Constructor.
     *
     * @param rsClass the class or interface containing JAX-RS annotations to use for mapping Java operations to REST resources.
     * @param uri     the source URI.
     * @param type    the authentication type
     */
    public RsWireSourceDefinition(String rsClass, URI uri, AuthenticationType type) {
        this.rsClass = rsClass;
        setUri(uri);
        this.authenticationType = type;
    }

    public String getRsClass() {
        return rsClass;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}
