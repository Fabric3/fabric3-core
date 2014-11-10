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
package org.fabric3.introspection.xml.common;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

import org.fabric3.api.model.type.component.Property;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Loads a property declaration in a composite or on a component.
 */
public class PropertyLoader extends AbstractValidatingTypeLoader<Property> {
    private static final String NAME = "name";
    private static final String MANY = "many";
    private static final String MUST_SUPPLY = "mustSupply";
    private static final String TYPE = "type";
    private static final String ELEMENT = "element";
    private static final String SOURCE = "source";
    private static final String VALUE = "value";

    private LoaderHelper helper;
    private boolean roundTrip;

    public PropertyLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        addAttributes(NAME, MANY, MUST_SUPPLY, TYPE, SOURCE, ELEMENT, VALUE);
    }

    @org.oasisopen.sca.annotation.Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    public Property load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, NAME);
        String manyAttr = reader.getAttributeValue(null, MANY);
        boolean many = Boolean.parseBoolean(manyAttr);
        String mustSupplyAttr = reader.getAttributeValue(null, MUST_SUPPLY);
        boolean mustSupply = Boolean.parseBoolean(mustSupplyAttr);
        String typeAttribute = reader.getAttributeValue(null, TYPE);
        String elementAttribute = reader.getAttributeValue(null, ELEMENT);

        Property property = new Property(name);

        validateAttributes(reader, context, property);

        if (typeAttribute != null && elementAttribute != null) {
            InvalidAttributes error =
                    new InvalidAttributes("Cannot specify both type and element attributes for a property", startLocation, property);
            context.addError(error);
        }

        QName type = null;
        QName element = null;
        if (typeAttribute != null) {
            try {
                type = helper.createQName(typeAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property type namespace:" + e.getMessage(), startLocation, property);
                context.addError(error);
            }
        } else if (elementAttribute != null) {
            try {
                element = helper.createQName(elementAttribute, reader);
            } catch (InvalidPrefixException e) {
                InvalidAttributes error = new InvalidAttributes("Invalid property element namespace:" + e.getMessage(), startLocation, property);
                context.addError(error);
            }
        }
        String valueAttribute = reader.getAttributeValue(null, VALUE);

        Document value = helper.loadPropertyValues(reader);

        if (valueAttribute != null && value.getDocumentElement().getChildNodes().getLength() > 0) {
            InvalidPropertyValue error =
                    new InvalidPropertyValue("Property value configured using the value attribute and inline: " + name, startLocation, property);
            context.addError(error);
        }
        if (roundTrip) {
            property.enableRoundTrip();
            if (manyAttr != null) {
                property.attributeSpecified(MANY);
            }
            if (mustSupplyAttr != null) {
                property.attributeSpecified(MUST_SUPPLY);
            }
            if (typeAttribute != null) {
                property.attributeSpecified(TYPE);
            }
            if (elementAttribute != null) {
                property.attributeSpecified(ELEMENT);
            }
            if (valueAttribute != null) {
                property.attributeSpecified(VALUE);
            }

        }

        property.setRequired(mustSupply);
        property.setType(type);
        property.setElement(element);
        property.setMany(many);
        if (!many && value.getDocumentElement().getChildNodes().getLength() > 1) {
            InvalidPropertyValue error =
                    new InvalidPropertyValue("A single-valued property is configured with multiple values: " + name, startLocation, property);
            context.addError(error);
        } else {
            if (valueAttribute != null) {
                value = helper.loadPropertyValue(valueAttribute);
            }
            property.setDefaultValue(value);
        }

        return property;
    }

}
