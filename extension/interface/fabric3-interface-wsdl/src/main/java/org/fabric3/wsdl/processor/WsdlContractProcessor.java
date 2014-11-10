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
package org.fabric3.wsdl.processor;

import javax.wsdl.Definition;
import javax.wsdl.PortType;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Introspects a WSDL port type and returns a corresponding WsdlServiceContract.
 */
public interface WsdlContractProcessor {

    /**
     * Creates a WsdlServiceContract by introspecting the WSDL 1.1 port type.
     *
     * @param portType         the WSDL 1.1 port type
     * @param definition       the WSDL document containing the port type
     * @param schemaCollection the schemas contained in or referenced by the WSDL containing the port type
     * @param context          the context to report errors against
     * @return the list of operations.
     */
    WsdlServiceContract introspect(PortType portType, Definition definition, XmlSchemaCollection schemaCollection, IntrospectionContext context);

}
