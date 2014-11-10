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
package org.fabric3.implementation.spring.introspection;

import javax.xml.stream.XMLStreamException;

import org.fabric3.api.host.stream.Source;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Introspects a component type from a Spring application context.
 */
public interface SpringImplementationProcessor {

    /**
     * Introspects the component type from a Spring application context
     *
     * @param source  the application context source
     * @param context the context for reporting errors
     * @return the introspected component type
     * @throws XMLStreamException if an introspection error occurs
     */
    SpringComponentType introspect(Source source, IntrospectionContext context) throws XMLStreamException;
}