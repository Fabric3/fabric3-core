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
package org.fabric3.fabric.xml;

import org.fabric3.api.host.Fabric3Exception;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Loads XML documents as DOM objects.
 */
public interface DocumentLoader {

    /**
     * Loads a Document from an InputSource.
     *
     * @param source          the source of the document text
     * @param stripWhitespace true if whitespace should be stripped from the document
     * @return the content as a Document
     * @throws Fabric3Exception if there was a problem with the document
     */
    Document load(InputSource source, boolean stripWhitespace) throws Fabric3Exception;

    /**
     * Recursively add a namespace to a node. Note that the namespace is not added to attributes.
     *
     * @param document  the document containing the node
     * @param node      the node
     * @param namespace the namespace
     */
    void addNamespace(Document document, Node node, String namespace);

}
