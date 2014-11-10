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

import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.ComponentReference;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * Serializes a composite reference to a StAX stream.
 */
@EagerInit
public class ComponentReferenceWriter extends AbstractTypeWriter<ComponentReference> {

    public ComponentReferenceWriter(@Reference Writer writer) {
        super(ComponentReference.class, writer);
    }

    public void write(ComponentReference reference, XMLStreamWriter xmlWriter) throws XMLStreamException, UnrecognizedTypeException {
        xmlWriter.writeStartElement("reference");
        xmlWriter.writeAttribute("name", reference.getName());

        // TODO handle multiplicity

        StringBuilder targets = new StringBuilder();
        for (Target target : reference.getTargets()) {
            if (targets.length() > 0) {
                targets.append(",");
            }
            targets.append(target.getComponent());
            if (target.getBindable() != null) {
                targets.append("/").append(target.getBindable());
                if (target.getBinding() != null) {
                    targets.append("/").append(target.getBinding());
                }
            }
        }
        if (targets.length() > 0) {
            xmlWriter.writeAttribute("target", targets.toString());
        }
        List<ModelObject> elementStack = reference.getElementStack();
        for (ModelObject modelObject : elementStack) {
            writer.write(modelObject, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }


}
