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
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Serializes a composite to a StAX stream.
 */
@EagerInit
public class ComponentWriter extends AbstractTypeWriter<ComponentDefinition> {

    public ComponentWriter(@Reference Writer writer) {
        super(ComponentDefinition.class, writer);
    }

    @SuppressWarnings("unchecked")
    public void write(ComponentDefinition definition, XMLStreamWriter xmlWriter) throws XMLStreamException, UnrecognizedTypeException {
        xmlWriter.writeStartElement("component");
        xmlWriter.writeAttribute("name", definition.getName());
        // todo autowire, requires, policy sets, key
        List<ModelObject> elementStack = definition.getElementStack();
        for (ModelObject modelObject : elementStack) {
            writer.write(modelObject, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

}
