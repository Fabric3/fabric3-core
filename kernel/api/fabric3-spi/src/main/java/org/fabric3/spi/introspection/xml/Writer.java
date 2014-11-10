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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.api.model.type.ModelObject;

/**
 * Serializes model objects to a StAX output stream by delegating to {@link TypeWriter}s.
 */
public interface Writer {

    /**
     * Registers the {@link TypeWriter} for the given model object type.
     *
     * @param type   the model object type
     * @param writer the writer
     * @param <TYPE> the instance type
     */
    <TYPE extends ModelObject> void register(Class<TYPE> type, TypeWriter<TYPE> writer);

    /**
     * De-registers the {@link TypeWriter} for the given model object type.
     *
     * @param type   the model object type
     * @param <TYPE> the instance type
     */
    <TYPE extends ModelObject> void unregister(Class<TYPE> type);

    /**
     * Serializes a model object instance to an XML stream.
     *
     * @param type      the type instance to serialize
     * @param xmlWriter the XML stream writer
     * @throws XMLStreamException        if an error reading the XML stream occurs
     * @throws UnrecognizedTypeException if the type or a contained type is not recognized
     */
    <TYPE extends ModelObject> void write(TYPE type, XMLStreamWriter xmlWriter) throws XMLStreamException, UnrecognizedTypeException;
}
