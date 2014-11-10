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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;

import org.fabric3.api.model.type.PolicyAware;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Helper service for handling XML.
 */
public interface LoaderHelper {
    /**
     * Load the value of the attribute key from the current element.
     *
     * @param reader a stream containing a property value
     * @return the key value
     */
    String loadKey(XMLStreamReader reader);

    /**
     * Loads one or more property values configured in a composite or on a component from a Stax stream. Each property value is returned as child of
     * the document root.
     * <p/>
     * The reader must be positioned at the composite or component &lt;property&gt; element.
     *
     * @param reader the stream reader
     * @return a  document containing the values
     * @throws XMLStreamException if there was a problem reading the stream
     */
    Document loadPropertyValues(XMLStreamReader reader) throws XMLStreamException;

    /**
     * Loads a property value configured in a composite or on a component using the @value attribute from a String.
     *
     * @param content String content
     * @return a document containing the values
     * @throws XMLStreamException if there was a problem reading the stream
     */
    Document loadPropertyValue(String content) throws XMLStreamException;

    /**
     * Loads policy sets and intents. Errors will be collated in the IntrospectionContext.
     *
     * @param policyAware Element against which policy sets and intents are declared.
     * @param reader      XML stream reader from where the attributes are read.
     * @param context     the introspection context.
     */
    void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader, IntrospectionContext context);


    /**
     * Convert a URI from a String form of <code>component/service</code> to a URI form of <code>component/service</code>.
     *
     * @param value the URI to convert
     * @return a URI where the fragment represents the service name
     * @throws URISyntaxException if the value is an invalid URI
     */
    URI parseUri(String value) throws URISyntaxException;

    /**
     * Convert a URI from a String form of <code>component/service/binding</code> to a Target.
     *
     * @param target the URI to convert
     * @param reader the stream reader parsing the XML document where the target is specified
     * @return a target instance
     * @throws InvalidTargetException if the target format is invalid
     */
    Target parseTarget(String target, XMLStreamReader reader) throws InvalidTargetException;

    /**
     * Parses a list of qualified names.
     *
     * @param reader    XML stream reader.
     * @param attribute Attribute that contains the list of qualified names.
     * @return Set containing the qualified names.
     * @throws InvalidPrefixException If the qualified name cannot be resolved.
     */
    Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException;

    /**
     * Constructs a QName from the given name. If a namespace prefix is not specified in the name, the namespace context is used.
     *
     * @param name   the name to parse
     * @param reader the XML stream reader
     * @return the parsed QName
     * @throws InvalidPrefixException if a specified namespace prefix is invalid
     */
    QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException;

    /**
     * Parses a list of URIs contained in a attribute.
     *
     * @param reader    the XML stream reader
     * @param attribute the attribute to parse
     * @return the list of URIs contained in that attribute, or null if the attribute is not present
     * @throws URISyntaxException if the attribute contains an invalid URI
     */
    List<URI> parseListOfUris(XMLStreamReader reader, String attribute) throws URISyntaxException;

    /**
     * Determines if the first multiplicity setting can narrow the second.
     *
     * @param first  multiplicity setting
     * @param second multiplicity setting
     * @return true if the first can narrow the second
     */
    boolean canNarrow(Multiplicity first, Multiplicity second);

    /**
     * Transforms the XML element to a DOM representation.
     *
     * @param reader the XML stream reader
     * @return the DOM
     * @throws XMLStreamException if a conversion exception is encountered
     */
    Document transform(XMLStreamReader reader) throws XMLStreamException;
}
