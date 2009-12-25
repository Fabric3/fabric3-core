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
package org.fabric3.introspection.xml.common;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.model.type.component.Property;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loads a property declaration from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class PropertyLoader implements TypeLoader<Property> {
    private static final String NAME = "name";
    private static final String MANY = "many";
    private static final String MUST_SUPPLY = "mustSupply";
    private static final String TYPE = "type";
    private static final String ELEMENT = "element";
    private static final String SOURCE = "source";

    private final LoaderHelper helper;

    public PropertyLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public Property load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, NAME);
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, MANY));
        boolean mustSupply = Boolean.parseBoolean(reader.getAttributeValue(null, MUST_SUPPLY));
        String type = reader.getAttributeValue(null, TYPE);
        String element = reader.getAttributeValue(null, ELEMENT);

        if (type != null && element != null ) {
             context.addError(new InvalidAtttributes("Cannot specify both type and element attributes for a property", reader));
        }
        Document value = helper.loadValue(reader);

        Property property = new Property(name);
        property.setRequired(mustSupply);
        property.setMany(many);
        property.setDefaultValue(value);

        return property;
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!NAME.equals(name) && !MANY.equals(name) && !MUST_SUPPLY.equals(name) && !TYPE.equals(name) && !SOURCE.equals(name)
                    && !ELEMENT.equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
