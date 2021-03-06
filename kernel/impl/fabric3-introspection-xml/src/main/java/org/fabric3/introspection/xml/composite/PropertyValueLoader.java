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
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.model.type.component.PropertyMany;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.introspection.xml.common.InvalidAttributes;
import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Loads property values configured on a component.
 */
public class PropertyValueLoader extends AbstractExtensibleTypeLoader<PropertyValue> {
    private static final QName PROPERTY = new QName(Constants.SCA_NS, "property");
    private final LoaderHelper helper;

    public PropertyValueLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        super(registry);
        addAttributes("name", "source", "file", "type", "element", "value", "many");
        this.helper = helper;
    }

    public QName getXMLType() {
        return PROPERTY;
    }

    public PropertyValue load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }

        String source = reader.getAttributeValue(null, "source");
        String file = reader.getAttributeValue(null, "file");
        String typeAttribute = reader.getAttributeValue(null, "type");
        QName type = null;
        if (source != null) {
            PropertyValue value = new PropertyValue(name, source);
            value.setType(type);
            if (typeAttribute != null) {
                try {
                    type = helper.createQName(typeAttribute, reader);
                    value.setType(type);
                } catch (InvalidPrefixException e) {
                    InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), startLocation, value);
                    context.addError(error);
                }
            }
            validateAttributes(reader, context, value);
            LoaderUtil.skipToEndElement(reader);
            return value;
        } else if (file != null) {
            try {
                URI uri = new URI(file);
                if (!uri.isAbsolute()) {
                    uri = context.getSourceBase().toURI().resolve(uri);
                }
                PropertyValue value = new PropertyValue(name, uri);
                if (typeAttribute != null) {
                    try {
                        type = helper.createQName(typeAttribute, reader);
                    } catch (InvalidPrefixException e) {
                        InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), startLocation, value);
                        context.addError(error);
                    }
                }
                value.setType(type);
                validateAttributes(reader, context, value);
                LoaderUtil.skipToEndElement(reader);
                return value;
            } catch (URISyntaxException e) {
                InvalidValue failure = new InvalidValue("File specified for property " + name + " is invalid: " + file, startLocation, e);
                context.addError(failure);
                return null;
            }
        } else {
            return loadInlinePropertyValue(name, reader, startLocation, context);
        }
    }

    private PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader, Location location, IntrospectionContext context)
            throws XMLStreamException {
        String typeAttribute = reader.getAttributeValue(null, "type");
        String elementAttribute = reader.getAttributeValue(null, "element");
        PropertyMany many = parseMany(reader);
        String valueAttribute = reader.getAttributeValue(null, "value");

        PropertyValue propertyValue = new PropertyValue(name, many);

        validateAttributes(reader, context, propertyValue);

        Document value = helper.loadPropertyValues(reader);

        if (valueAttribute != null) {
            NodeList childNodes = value.getDocumentElement().getChildNodes();
            if (valueAttribute != null && childNodes.getLength() > 0 && childNodes.item(0).getTextContent().length() > 0) {
                InvalidPropertyValue error = new InvalidPropertyValue("Property value configured using a value attribute and inline: " + name,
                                                                      location,
                                                                      propertyValue);
                context.addError(error);
                return propertyValue;
            }
            value = helper.loadPropertyValue(valueAttribute);
        }
        propertyValue.setValue(value);

        QName type = null;
        QName element = null;
        if (typeAttribute != null) {
            if (elementAttribute != null) {
                InvalidValue failure = new InvalidValue("Cannot supply both type and element for property: " + name, location);
                context.addError(failure);
            }
            try {
                type = helper.createQName(typeAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), location, propertyValue);
                context.addError(error);
            }
        } else if (elementAttribute != null) {
            try {
                element = helper.createQName(elementAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property element namespace:" + e.getMessage(), location, propertyValue);
                context.addError(error);
            }
        }
        propertyValue.setElement(element);
        propertyValue.setType(type);

        return propertyValue;
    }

    private PropertyMany parseMany(XMLStreamReader reader) {
        String manyAttribute = reader.getAttributeValue(null, "many");
        if (manyAttribute == null) {
            return PropertyMany.NOT_SPECIFIED;
        } else if (manyAttribute.equalsIgnoreCase("true")) {
            return PropertyMany.MANY;
        }
        return PropertyMany.SINGLE;
    }

}
