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
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Processes an XML-based contribution.
 */
public interface XmlProcessor {

    /**
     * Returns the QName for the type of XML contribution handled by this processor.
     *
     * @return the QName
     */
    QName getType();

    /**
     * Processes the XML contribution.
     *
     * @param contribution the contribution metadata to update
     * @param reader       the reader positioned at the first element of the document
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs processing
     */
    void processContent(Contribution contribution, XMLStreamReader reader, IntrospectionContext context) throws InstallException;
}
