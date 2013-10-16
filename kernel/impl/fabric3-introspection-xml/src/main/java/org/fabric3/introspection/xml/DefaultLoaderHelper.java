/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import org.fabric3.api.host.Namespaces;
import org.fabric3.api.model.type.PolicyAware;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.spi.deployment.generator.policy.PolicyRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.api.model.type.component.Multiplicity.ONE_N;
import static org.fabric3.api.model.type.component.Multiplicity.ONE_ONE;
import static org.fabric3.api.model.type.component.Multiplicity.ZERO_ONE;

/**
 * Default implementation of the loader helper.
 */
public class DefaultLoaderHelper implements LoaderHelper {
    private DocumentBuilderFactory documentBuilderFactory;
    private PolicyRegistry policyRegistry;
    private boolean strictValidation;

    public DefaultLoaderHelper() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    @Constructor
    public DefaultLoaderHelper(@Reference PolicyRegistry policyRegistry) {
        this();
        this.policyRegistry = policyRegistry;
    }

    @Property(required = false)
    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }

    public String loadKey(XMLStreamReader reader) {
        String key = reader.getAttributeValue(Namespaces.F3, "key");
        if (key == null) {
            return null;
        }

        int index = key.indexOf(':');
        if (index != -1 && !key.startsWith("{")) {
            // treat the key as a QName
            String prefix = key.substring(0, index);
            String localPart = key.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            key = "{" + ns + "}" + localPart;
        }
        return key;
    }


    public void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader, IntrospectionContext context) {
        try {
            Set<QName> intentNames = parseListOfQNames(reader, "requires");
            validateIntents(intentNames, reader, context);
            policyAware.setIntents(intentNames);
            policyAware.setPolicySets(policySets(reader));

        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            Location location = reader.getLocation();
            InvalidQNamePrefix failure =
                    new InvalidQNamePrefix("The prefix " + prefix + " specified in contribution " + uri + " is invalid", location);
            context.addError(failure);
        }
    }

    public Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException {
        Set<QName> qNames = new HashSet<QName>();
        String val = reader.getAttributeValue(null, attribute);
        if (val != null) {
            StringTokenizer tok = new StringTokenizer(val);
            while (tok.hasMoreElements()) {
                qNames.add(createQName(tok.nextToken(), reader));
            }
        }
        return qNames;
    }

    public QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException {
        QName qName;
        int index = name.indexOf(':');
        if (index != -1) {
            String prefix = name.substring(0, index);
            String localPart = name.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                throw new InvalidPrefixException("Invalid prefix: " + prefix, prefix, reader);
            }
            qName = new QName(ns, localPart, prefix);
        } else {
            String prefix = "";
            String ns = reader.getNamespaceURI();
            qName = new QName(ns, name, prefix);
        }
        return qName;
    }

    public URI parseUri(String target) throws URISyntaxException {
        if (target == null) {
            return null;
        }

        int index = target.lastIndexOf('/');
        if (index == -1) {
            return new URI(target);
        } else {
            String uri = target.substring(0, index);
            String fragment = target.substring(index + 1);
            return new URI(uri + '#' + fragment);
        }
    }

    public Target parseTarget(String target, XMLStreamReader reader) throws InvalidTargetException {
        if (target == null) {
            return null;
        }
        String[] tokens = target.split("/");
        if (tokens.length == 1) {
            return new Target(tokens[0]);
        } else if (tokens.length == 2) {
            return new Target(tokens[0], tokens[1]);
        } else if (tokens.length == 3) {
            return new Target(tokens[0], tokens[1], tokens[2]);
        } else {
            throw new InvalidTargetException("Invalid target format: " + target, target, reader);
        }
    }

    public List<URI> parseListOfUris(XMLStreamReader reader, String attribute) throws URISyntaxException {
        String value = reader.getAttributeValue(null, attribute);
        if (value == null || value.length() == 0) {
            return null;
        } else {
            StringTokenizer tok = new StringTokenizer(value);
            List<URI> result = new ArrayList<URI>(tok.countTokens());
            while (tok.hasMoreTokens()) {
                result.add(parseUri(tok.nextToken().trim()));
            }
            return result;
        }
    }

    public boolean canNarrow(Multiplicity first, Multiplicity second) {
        switch (second) {
        case ONE_ONE:
            return ONE_ONE == first;
        case ONE_N:
            return ONE_ONE == first || ONE_N == first;
        case ZERO_N:
            return true;
        case ZERO_ONE:
            return ONE_ONE == first || ZERO_ONE == first;
        }
        return false;

    }

    public Document loadPropertyValues(XMLStreamReader reader) throws XMLStreamException {
        Document document = createDocument();

        int depth = 0;
        Element root = document.createElementNS("", "values");
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            root.setAttributeNS(reader.getAttributeNamespace(i),
                                reader.getAttributeLocalName(i),
                                reader.getAttributeValue(i));
        }
        populateNamespaces(reader, root);
        document.appendChild(root);
        Node element = root;

        while (true) {
            int next = reader.next();
            switch (next) {
            case START_ELEMENT:
                String namespace = reader.getNamespaceURI();
                String name = reader.getLocalName();

                if (depth == 0) {
                    if (!"value".equals(name)) {
                        element = document.getDocumentElement();
                    }
                }

                Element child = document.createElementNS(namespace, name);

                if (element != null) {
                    element.appendChild(child);
                } else {
                    document.appendChild(child);
                }
                int count = reader.getAttributeCount();
                for (int i = 0; i < count; i++) {
                    String attrNamespace = reader.getAttributeNamespace(i);
                    String attrName = reader.getAttributeLocalName(i);
                    String attrValue = reader.getAttributeValue(i);
                    if (attrNamespace == null) {
                        child.setAttribute(attrName, attrValue);
                    } else {
                        child.setAttributeNS(attrNamespace, attrName, attrValue);
                    }
                }
                element = child;
                depth++;
                break;
            case CHARACTERS:
            case CDATA:
                String value = reader.getText();
                if (value.trim().length() == 0) {
                    // empty, skip node
                    break;
                }
                if (depth == 0) {
                    // simple value, e.g. <property..>val</property>
                    element = document.createElement("value");
                    root.appendChild(element);
                }
                Text text = document.createTextNode(value);
                element.appendChild(text);
                break;
            case END_ELEMENT:
                QName elementName = reader.getName();
                String localPart = elementName.getLocalPart();
                String ns = elementName.getNamespaceURI();
                if (localPart.equals("property") && ("".equals(ns) || Constants.SCA_NS.equals(ns))) {
                    return document;
                }
                depth--;
                if (depth == 0) {
                    // property has multiple values, reset the current element and document
                    element = root;
                } else {
                    element = element.getParentNode();
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                return document;

            case ENTITY_REFERENCE:
            case COMMENT:
            case SPACE:
            case PROCESSING_INSTRUCTION:
            case DTD:
                break;
            }
        }
    }

    public Document loadPropertyValue(String content) throws XMLStreamException {
        Document document = createDocument();

        Element root = document.createElement("values");
        document.appendChild(root);
        Element element = document.createElement("value");
        root.appendChild(element);
        Text text = document.createTextNode(content);
        element.appendChild(text);
        return document;
    }


    public Document transform(XMLStreamReader reader) throws XMLStreamException {

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException("The stream needs to be at the start of an element");
        }

        Document document = createDocument();

        QName rootName = reader.getName();
        Element root = createElement(reader, document, rootName);

        document.appendChild(root);

        while (true) {

            int next = reader.next();
            switch (next) {
            case START_ELEMENT:

                QName childName = new QName(reader.getNamespaceURI(), reader.getLocalName());
                Element child = createElement(reader, document, childName);

                root.appendChild(child);
                root = child;

                break;

            case CHARACTERS:
            case CDATA:
                Text text = document.createTextNode(reader.getText());
                root.appendChild(text);
                break;
            case END_ELEMENT:
                if (rootName.equals(reader.getName())) {
                    return document;
                }
                root = (Element) root.getParentNode();
            case ENTITY_REFERENCE:
            case COMMENT:
            case SPACE:
            case PROCESSING_INSTRUCTION:
            case DTD:
                break;
            }
        }
    }

    private void validateIntents(Set<QName> intentNames, XMLStreamReader reader, IntrospectionContext context) {
        if (!strictValidation || policyRegistry == null) {
            return;
        }
        Set<QName> excluded = new HashSet<QName>();
        // check for mutually exclusive intents
        for (QName name : intentNames) {
            Intent intent = policyRegistry.getDefinition(name, Intent.class);
            if (!intent.getExcludes().isEmpty()) {
                for (QName exclude : intent.getExcludes()) {
                    if (excluded.contains(exclude) || intentNames.contains(exclude)) {
                        Location location = reader.getLocation();
                        InvalidValue error = new InvalidValue("Mutually exclusive intents configured: " + exclude, location);
                        context.addError(error);
                    } else {
                        excluded.add(exclude);
                    }
                }
            }
        }
    }

    private Set<QName> policySets(XMLStreamReader reader) throws InvalidPrefixException {
        return parseListOfQNames(reader, "policySets");
    }

    private void populateNamespaces(XMLStreamReader reader, Element element) {
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            String prefix = reader.getNamespacePrefix(i);
            String uri = reader.getNamespaceURI(i);
            prefix = prefix == null || prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix;
            element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, uri);
        }
    }

    private Document createDocument() throws XMLStreamException {
        DocumentBuilder builder = getDocumentBuilder();
        return builder.newDocument();
    }

    private DocumentBuilder getDocumentBuilder() throws XMLStreamException {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Creates the element and populates namespace declarations and attributes.
     *
     * @param reader   the reader source
     * @param document the document to update
     * @param rootName the root element name
     * @return the element
     */
    private Element createElement(XMLStreamReader reader, Document document, QName rootName) {
        Element root = document.createElementNS(rootName.getNamespaceURI(), rootName.getLocalPart());

        // Handle namespace declarations
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            String prefix = reader.getNamespacePrefix(i);
            String uri = reader.getNamespaceURI(i);

            prefix = prefix == null ? "xmlns" : "xmlns:" + prefix;

            root.setAttribute(prefix, uri);
        }

        // Handle attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {

            String attributeNs = reader.getAttributeNamespace(i);
            String localName = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            String attributePrefix = reader.getAttributePrefix(i);
            String qualifiedName = attributePrefix == null || attributePrefix.length() == 0 ? localName : attributePrefix + ":" + localName;

            if (attributeNs == null || attributeNs.length() == 0) {
                root.setAttribute(qualifiedName, value);
            } else {
                root.setAttributeNS(attributeNs, qualifiedName, value);
            }

        }
        return root;
    }


}
