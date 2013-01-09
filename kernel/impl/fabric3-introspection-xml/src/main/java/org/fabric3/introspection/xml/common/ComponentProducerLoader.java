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
import org.fabric3.model.type.component.ComponentProducer;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component producer configuration.
 */
public class ComponentProducerLoader extends AbstractExtensibleTypeLoader<ComponentProducer> {
    private static final QName PRODUCER = new QName(SCA_NS, "producer");

    private boolean roundTrip;

    public ComponentProducerLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "target");
    }

    @Property(required = false)
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public QName getXMLType() {
        return PRODUCER;
    }

    public ComponentProducer load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingProducerName failure = new MissingProducerName(startLocation);
            context.addError(failure);
            return null;
        }

        String targetAttribute = reader.getAttributeValue(null, "target");
        ComponentProducer producer = new ComponentProducer(name);

        List<URI> targets = new ArrayList<URI>();
        if (targetAttribute != null) {
            StringTokenizer tokenizer = new StringTokenizer(targetAttribute);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                try {
                    if (token.startsWith("\\\\")) {
                        // a domain level channel
                        token = "domain://" + token.substring(2);
                    }
                    URI target = new URI(token);
                    targets.add(target);
                } catch (URISyntaxException e) {
                    InvalidValue failure = new InvalidValue("Invalid target format", startLocation, e);
                    context.addError(failure);
                }
            }
        }

        producer.setTargets(targets);

        validateAttributes(reader, context, producer);

        if (roundTrip) {
            producer.enableRoundTrip();
            //noinspection VariableNotUsedInsideIf
            if (targetAttribute != null) {
                producer.attributeSpecified("target");

            }
        }

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    producer.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    boolean check = BindingHelper.checkDuplicateNames(binding, producer.getBindings(), location, context);
                    if (check) {
                        producer.addBinding(binding);
                    }
                } else if (type == null) {
                    // no type, continue processing
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader, location, producer));
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;
            case END_ELEMENT:
                if (PRODUCER.equals(reader.getName())) {
                    return producer;
                }
            }
        }
    }


}