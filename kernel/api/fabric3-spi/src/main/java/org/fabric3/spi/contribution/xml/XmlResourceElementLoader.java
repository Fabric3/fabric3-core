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
package org.fabric3.spi.contribution.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Loads the value of a ResourceElement from an XML artifact.
 */
public interface XmlResourceElementLoader {

    /**
     * Returns the QName of the element type this loader handles.
     *
     * @return the QName of the element type this loader handles
     */
    QName getType();

    /**
     * Loads the element.
     *
     * @param reader   the reader positioned on the first element
     * @param resource the resource that contains the element
     * @param context  the context to which validation errors and warnings are reported
     * @throws ContainerException   if a general load error occurs
     * @throws XMLStreamException if there is an error reading the XML stream
     */
    void load(XMLStreamReader reader, Resource resource, IntrospectionContext context) throws ContainerException, XMLStreamException;

}
