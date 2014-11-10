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

import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.xml.TypeWriter;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * Default {@link Writer} implementation.
 */
@EagerInit
public class WriterImpl implements Writer {
    private Map<Class<?>, TypeWriter<?>> typeWriters = new HashMap<>();

    public <TYPE extends ModelObject> void register(Class<TYPE> type, TypeWriter<TYPE> writer) {
        typeWriters.put(type, writer);
    }

    public <TYPE extends ModelObject> void unregister(Class<TYPE> type) {
        typeWriters.remove(type);
    }

    @SuppressWarnings({"unchecked"})
    public <TYPE extends ModelObject> void write(TYPE type, XMLStreamWriter xmlWriter) throws XMLStreamException, UnrecognizedTypeException {
        TypeWriter<TYPE> writer = (TypeWriter<TYPE>) typeWriters.get(type.getClass());
        if (writer == null) {
            throw new UnrecognizedTypeException(type.getClass());
        }
        writer.write(type, xmlWriter);
    }
}
