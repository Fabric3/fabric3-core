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
package org.fabric3.fabric.instantiator.component;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.xml.DocumentLoader;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Contains functionality common to different component instantiators.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractComponentInstantiator {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final XPathFactory XPATH_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        XPATH_FACTORY = XPathFactory.newInstance();
    }

    private final DocumentLoader documentLoader;

    protected AbstractComponentInstantiator(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    /**
     * Set the initial actual property values of a component.
     *
     * @param component  the component to initialize
     * @param definition the definition of the component
     * @param context    the instantiation conte
     */
    protected void initializeProperties(LogicalComponent<?> component, ComponentDefinition<?> definition, InstantiationContext context) {

        Map<String, PropertyValue> propertyValues = definition.getPropertyValues();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getComponentType();

        for (Property property : componentType.getProperties().values()) {

            String name = property.getName();
            PropertyValue propertyValue = propertyValues.get(name);
            Document value;

            if (propertyValue == null) {
                // use default value from component type
                value = property.getDefaultValue();
            } else {
                // the spec defines the following sequence
                if (propertyValue.getFile() != null) {
                    // load the value from an external resource
                    value = loadValueFromFile(component, property.getName(), propertyValue.getFile(), context);
                } else if (propertyValue.getSource() != null) {
                    // get the value by evaluating an XPath against the composite properties
                    try {
                        value = deriveValueFromXPath(propertyValue.getSource(), component.getParent(), propertyValue.getNamespaceContext());
                    } catch (XPathExpressionException e) {
                        URI uri = component.getUri();
                        URI contributionUri = component.getDefinition().getContributionUri();
                        InvalidProperty error = new InvalidProperty(name, e, uri, contributionUri);
                        context.addError(error);
                        return;
                    }
                } else {
                    // use inline XML file
                    value = propertyValue.getValue();
                }

            }
            if (property.isRequired() && value == null) {
                // The XPath expression returned an empty value. Since the property is required, throw an exception
                URI uri = component.getUri();
                URI contributionUri = component.getDefinition().getContributionUri();
                PropertySourceNotFound error = new PropertySourceNotFound(name, uri, contributionUri);
                context.addError(error);
            } else if (!property.isRequired() && value == null) {
                // The XPath expression returned an empty value. Since the property is optional, ignore it
                continue;
            } else {
                // set the property value
                component.setPropertyValue(name, value);
            }

        }

    }

    Document deriveValueFromXPath(String source, final LogicalComponent<?> parent, NamespaceContext nsContext) throws XPathExpressionException {

        XPathVariableResolver variableResolver = new XPathVariableResolver() {
            public Object resolveVariable(QName qName) {
                String name = qName.getLocalPart();
                Document value = parent.getPropertyValue(name);
                if (value == null) {
                    return null;
                }
                return value.getDocumentElement();
            }
        };

        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setXPathVariableResolver(variableResolver);
        xpath.setNamespaceContext(nsContext);
        
        DocumentBuilder builder;
        try {
            builder = DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }

        Document value = builder.newDocument();
        Element root = value.createElement("value");
        // TODO do we need to copy namespace declarations to this root
        value.appendChild(root);
        try {
            NodeList result = (NodeList) xpath.evaluate(source, root, XPathConstants.NODESET);
            if (result.getLength() == 0) {
                return null;
            }
            for (int i = 0; i < result.getLength(); i++) {
                Node node = result.item(i);
                // clone the node as the original may be accessed multiple times
                Node cloned = node.cloneNode(true);
                value.adoptNode(cloned);
                short type = cloned.getNodeType();
                if (Node.ELEMENT_NODE == type || Node.TEXT_NODE == type) {
                    root.appendChild(cloned);
                } else if (Node.ATTRIBUTE_NODE == type) {
                    // convert the attribute to an element in the property DOM
                    Element element = value.createElement(cloned.getNodeName());
                    element.setTextContent(cloned.getNodeValue());
                    root.appendChild(element);
                } else {
                    throw new XPathExpressionException("Unsupported node type: " + type);
                }
            }
        } catch (XPathExpressionException e) {
            // FIXME rethrow this for now, fix if people find it confusing
            // the Apache and Sun implementations of XPath throw a nested NullPointerException
            // if the xpath contains an unresolvable variable. It might be better to throw
            // a more descriptive cause, but that also might be confusing for people who
            // are used to this behaviour
            throw e;
        }
        return value;

    }

    private Document loadValueFromFile(LogicalComponent<?> parent, String name, URI file, InstantiationContext context) {
        try {
            return documentLoader.load(file);
        } catch (IOException e) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            InvalidPropertyFile error = new InvalidPropertyFile(name, e, file, uri, contributionUri);
            context.addError(error);
            return null;
        } catch (SAXException e) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            InvalidPropertyFile error = new InvalidPropertyFile(name, e, file, uri, contributionUri);
            context.addError(error);
            return null;
        }
    }


}
