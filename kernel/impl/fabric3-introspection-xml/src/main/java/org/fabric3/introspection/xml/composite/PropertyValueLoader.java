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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.model.type.xsd.XSDConstants;

/**
 * Loads property values configured on a component.
 *
 * @version $Rev$ $Date$
 */
public class PropertyValueLoader extends AbstractExtensibleTypeLoader<PropertyValue> {
    private static final QName PROPERTY = new QName(Constants.SCA_NS, "property");
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("source", "source");
        ATTRIBUTES.put("file", "file");
        ATTRIBUTES.put("type", "type");
        ATTRIBUTES.put("element", "element");
        ATTRIBUTES.put("value", "value");
    }

    private final LoaderHelper helper;

    public PropertyValueLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        super(registry);
        this.helper = helper;
    }

    public QName getXMLType() {
        return PROPERTY;
    }

    public PropertyValue load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", reader);
            context.addError(failure);
            return null;
        }

        String source = reader.getAttributeValue(null, "source");
        String file = reader.getAttributeValue(null, "file");
        if (source != null) {
            LoaderUtil.skipToEndElement(reader);
            return new PropertyValue(name, source);
        } else if (file != null) {
            try {
                URI uri = new URI(file);
                if (!uri.isAbsolute()) {
                    uri = context.getSourceBase().toURI().resolve(uri);
                }
                LoaderUtil.skipToEndElement(reader);
                return new PropertyValue(name, uri);
            } catch (URISyntaxException e) {
                InvalidValue failure = new InvalidValue("File specified for property " + name + " is invalid: " + file, reader, e);
                context.addError(failure);
                return null;
            }
        } else {
            return loadInlinePropertyValue(name, reader, context);
        }
    }

    private PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        DataType<QName> dataType;
        String type = reader.getAttributeValue(null, "type");
        String element = reader.getAttributeValue(null, "element");
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, "many"));
        String valueAttribute = reader.getAttributeValue(null, "value");

        if (type != null) {
            if (element != null) {
                InvalidValue failure = new InvalidValue("Cannot supply both type and element for property: " + name, reader);
                context.addError(failure);
                return null;
            }
            dataType = XSDConstants.PROPERTY_TYPE;
            // TODO support type attribute
//            throw new UnsupportedOperationException();
        } else if (element != null) {
            // TODO support element attribute
            dataType = XSDConstants.PROPERTY_TYPE;
//            throw new UnsupportedOperationException();
        } else {
            dataType = XSDConstants.PROPERTY_TYPE;
        }

        List<Document> values = helper.loadPropertyValues(reader);

        if (valueAttribute != null && values.size() > 0) {
            InvalidPropertyValue error = new InvalidPropertyValue("Property value configured using the value attribute and inline: " + name, reader);
            context.addError(error);
        }

        if (!many && values.size() > 1) {
            InvalidPropertyValue error = new InvalidPropertyValue("A single-valued property is configured with multiple values: " + name, reader);
            context.addError(error);
        } else {
            if (valueAttribute != null) {
                values = helper.loadPropertyValue(valueAttribute, reader);
            }
        }

        return new PropertyValue(name, dataType, values);

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
