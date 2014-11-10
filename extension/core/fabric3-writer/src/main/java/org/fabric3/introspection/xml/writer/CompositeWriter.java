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
package org.fabric3.introspection.xml.writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.Namespace;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.introspection.xml.CompositeConstants;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * Serializes a composite to a StAX stream.
 */
@EagerInit
public class CompositeWriter extends AbstractTypeWriter<Composite> {

    public CompositeWriter(@Reference Writer writer) {
        super(Composite.class, writer);
    }

    public void write(Composite composite, XMLStreamWriter xmlWriter) throws XMLStreamException, UnrecognizedTypeException {
        xmlWriter.setDefaultNamespace(Constants.SCA_NS);
        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement(CompositeConstants.COMPOSITE.getLocalPart());
        for (Namespace namespace : composite.getNamespaces()) {
            xmlWriter.writeNamespace(namespace.getPrefix(), namespace.getUri());
        }
        xmlWriter.writeAttribute("targetNamespace", composite.getName().getNamespaceURI());
        xmlWriter.writeAttribute("name", composite.getName().getLocalPart());

        for (ModelObject modelObject : composite.getElementStack()) {
            writer.write(modelObject, xmlWriter);
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

}
