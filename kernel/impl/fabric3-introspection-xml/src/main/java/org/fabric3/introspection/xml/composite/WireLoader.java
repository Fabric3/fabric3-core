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
package org.fabric3.introspection.xml.composite;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.component.WireDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;

/**
 *
 */
@EagerInit
public class WireLoader extends AbstractValidatingTypeLoader<WireDefinition> {
    private LoaderHelper helper;
    private boolean roundTrip;

    public WireLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        addAttributes("source", "target", "requires", "replace");
    }

    @Property(required = false)
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    public WireDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String referenceAttribute = reader.getAttributeValue(null, "source");
        String serviceAttribute = reader.getAttributeValue(null, "target");
        String replaceAttribute = reader.getAttributeValue(null, "replace");
        boolean replace = Boolean.parseBoolean(replaceAttribute);

        Target referenceTarget = null;
        Target serviceTarget = null;
        try {
            referenceTarget = helper.parseTarget(referenceAttribute, reader);
            serviceTarget = helper.parseTarget(serviceAttribute, reader);
        } catch (InvalidTargetException e) {
            InvalidValue failure = new InvalidValue("Invalid wire attribute", startLocation, e);
            context.addError(failure);
        }
        WireDefinition definition = new WireDefinition(referenceTarget, serviceTarget, replace);
        if (roundTrip) {
            definition.enableRoundTrip();
            if (referenceAttribute != null) {
                definition.attributeSpecified("source");
            }
            if (serviceAttribute != null) {
                definition.attributeSpecified("target");
            }
            if (replaceAttribute != null) {
                definition.attributeSpecified("replace");
            }
        }

        validateAttributes(reader, context, definition);
        LoaderUtil.skipToEndElement(reader);
        return definition;
    }

}
