/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
*/
package org.fabric3.introspection.xml.definitions;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.definitions.PolicyPhase;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.transform.TransformationException;

/**
 * Loader for definitions.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class PolicySetLoader implements TypeLoader<PolicySet> {

    private static final DocumentBuilderFactory FACTORY;

    static {
        FACTORY = DocumentBuilderFactory.newInstance();
        FACTORY.setNamespaceAware(true);
    }

    private final LoaderHelper helper;

    public PolicySetLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PolicySet load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        Element policyElement;
        try {
            policyElement = transform(reader).getDocumentElement();
        } catch (TransformationException e) {
            DefinitionProcessingFailure failure = new DefinitionProcessingFailure("Error processing policy set", e, reader);
            context.addError(failure);
            return null;
        }

        String name = policyElement.getAttribute("name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> provides = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(policyElement.getAttribute("provides"));
        while (tok.hasMoreElements()) {
            try {
                provides.add(helper.createQName(tok.nextToken(), reader));
            } catch (InvalidPrefixException e) {
                String prefix = e.getPrefix();
                URI uri = context.getContributionUri();
                context.addError(new InvalidQNamePrefix("The prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                        + " is invalid", reader));
                return null;
            }
        }

        String appliesTo = policyElement.getAttribute("appliesTo");
        String attachTo = policyElement.getAttribute("attachTo");

        Element extension = null;
        NodeList children = policyElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                extension = (Element) children.item(i);
                break;
            }
        }

        // Determine the phase: if the policy language is in the F3 namespace, default to interception phase. Otherwise default to provided phase.
        PolicyPhase phase = PolicyPhase.PROVIDED;
        if (extension != null && Namespaces.POLICY.equals(extension.getNamespaceURI())) {
            String phaseAttr = extension.getAttributeNS(Namespaces.POLICY, "phase");
            if (phaseAttr != null && phaseAttr.length() > 0) {
                try {
                    phase = PolicyPhase.valueOf(phaseAttr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    UnrecognizedAttribute failure = new UnrecognizedAttribute("Invalid phase: " + phaseAttr, reader);
                    context.addError(failure);
                    return null;
                }

            } else {
                phase = PolicyPhase.INTERCEPTION;
            }
        }
        URI uri = context.getContributionUri();
        return new PolicySet(qName, provides, appliesTo, attachTo, extension, phase, uri);

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"provides".equals(name) && !"appliesTo".equals(name) && !"phase".equals(name) && !"attachTo".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    private Document transform(XMLStreamReader reader) throws TransformationException {

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new TransformationException("The stream needs to be at te start of an element");
        }

        DocumentBuilder builder = getDocumentBuilder();
        Document document = builder.newDocument();

        QName rootName = reader.getName();
        Element root = createElement(reader, document, rootName);

        document.appendChild(root);

        try {
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
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }

    }

    private DocumentBuilder getDocumentBuilder() throws TransformationException {
        try {
            return FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TransformationException(e);
        }
    }

    /*
     * Creates the element and populates the namespace declarations and attributes.
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
            String qualifiedName = attributePrefix == null ? localName : attributePrefix + ":" + localName;

            root.setAttributeNS(attributeNs, qualifiedName, value);

        }

        return root;

    }

}
    
