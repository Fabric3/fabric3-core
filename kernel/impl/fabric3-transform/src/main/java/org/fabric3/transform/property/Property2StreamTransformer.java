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
package org.fabric3.transform.property;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Node;

/**
 *
 */
public class Property2StreamTransformer implements SingleTypeTransformer<Node, XMLStreamReader> {
    private static final JavaType TARGET = new JavaType(XMLStreamReader.class);

    private final XMLInputFactory xmlFactory;

    public DataType getSourceType() {
        return XSDConstants.PROPERTY_TYPE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public Property2StreamTransformer(@Reference XMLFactory xmlFactory) {
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }


    public XMLStreamReader transform(Node element, ClassLoader loader) throws TransformationException {
        DOMSource source = new DOMSource(element);
        try {
            return xmlFactory.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }
}
