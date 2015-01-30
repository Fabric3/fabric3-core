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
package org.fabric3.implementation.java.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.xml.TypeWriter;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Serializes a Java component implementation to a StAX stream.
 */
@EagerInit
public class JavaImplementationWriter implements TypeWriter<JavaImplementation> {
    private Writer writer;

    @Reference(required = false)
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Init
    public void init() {
        if (writer != null) {
            writer.register(JavaImplementation.class, this);
        }
    }

    @Destroy
    public void destroy() {
        if (writer != null) {
            writer.unregister(JavaImplementation.class);
        }
    }

    public void write(JavaImplementation implementation, XMLStreamWriter writer) throws XMLStreamException, UnrecognizedTypeException {
        writer.writeStartElement("implementation.java");
        writer.writeAttribute("class", implementation.getImplementationClass());
        writer.writeEndElement();
    }
}
