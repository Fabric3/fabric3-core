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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
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
    @Source("$systemConfig/f3:loader/@round.trip")
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
