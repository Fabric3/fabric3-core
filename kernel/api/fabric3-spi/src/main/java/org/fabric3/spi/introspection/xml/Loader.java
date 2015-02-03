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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.host.stream.Source;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * System service for loading configuration artifacts from an XML source.
 */
public interface Loader {
    /**
     * Parse the supplied XML stream, dispatching to the appropriate registered loader for each element encountered in the stream.
     *
     * This method must be called with the XML cursor positioned on a START_ELEMENT event. When this method returns, the stream will be positioned on
     * the corresponding END_ELEMENT event.
     *
     * @param reader  the XML stream to parse
     * @param type    the type of Java object that should be returned
     * @param context the current loader context
     * @return the model object obtained by parsing the current element on the stream
     * @throws XMLStreamException           if there was a problem reading the stream
     * @throws ClassCastException           if the XML type cannot be cast to the expected output type
     */
    <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type, IntrospectionContext context) throws XMLStreamException;

    /**
     * Load a model object from a specified location.
     *
     * @param source  the source for the XML document to be loaded
     * @param type    the type of Java Object that should be returned
     * @param context the current loader context
     * @return the model object loaded from the document
     * @throws LoaderException    if there was a problem loading the document
     * @throws ClassCastException if the XML type cannot be cast to the expected output type
     */
    <OUTPUT> OUTPUT load(Source source, Class<OUTPUT> type, IntrospectionContext context) throws LoaderException;
}
