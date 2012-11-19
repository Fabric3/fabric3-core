/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentConsumer;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component consumer configuration.
 */
public class ComponentConsumerLoader extends AbstractExtensibleTypeLoader<ComponentConsumer> {
    private static final QName CONSUMER = new QName(SCA_NS, "consumer");

    private boolean roundTrip;

    public ComponentConsumerLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "source");
    }

    @Property(required = false)
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public QName getXMLType() {
        return CONSUMER;
    }

    public ComponentConsumer load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingProducerName failure = new MissingProducerName(startLocation);
            context.addError(failure);
            return null;
        }

        String targetAttribute = reader.getAttributeValue(null, "source");
        List<URI> sources = new ArrayList<URI>();
        ComponentConsumer consumer = new ComponentConsumer(name);
        try {
            if (targetAttribute != null) {
                StringTokenizer tokenizer = new StringTokenizer(targetAttribute);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    URI target = new URI(token);
                    sources.add(target);
                }
            }
        } catch (URISyntaxException e) {
            InvalidValue failure = new InvalidValue("Invalid source format", startLocation, e, consumer);
            context.addError(failure);
        }
        consumer.setSources(sources);

        validateAttributes(reader, context, consumer);

        if (roundTrip) {
            consumer.enableRoundTrip();
            //noinspection VariableNotUsedInsideIf
            if (targetAttribute != null) {
                consumer.attributeSpecified("source");
            }
        }

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                QName elementName = reader.getName();
                ModelObject type;
                type = registry.load(reader, ModelObject.class, context);
                if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    boolean check = BindingHelper.checkDuplicateNames(binding, consumer.getBindings(), location, context);
                    if (check) {
                        consumer.addBinding(binding);
                    }
                } else if (type == null) {
                    // no type, continue processing
                    continue;
                } else {
                    UnrecognizedElement failure = new UnrecognizedElement(reader, location);
                    context.addError(failure);
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;


            case END_ELEMENT:
                if (CONSUMER.equals(reader.getName())) {
                    return consumer;
                }
            }
        }
    }

}