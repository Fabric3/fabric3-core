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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ConsumerDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component consumer configuration.
 */
public class ComponentConsumerLoader extends AbstractExtensibleTypeLoader<ConsumerDefinition<ComponentDefinition>> {
    private static final QName CONSUMER = new QName(SCA_NS, "consumer");

    public ComponentConsumerLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "source");
    }

    public QName getXMLType() {
        return CONSUMER;
    }

    public ConsumerDefinition<ComponentDefinition> load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingProducerName failure = new MissingProducerName(startLocation);
            context.addError(failure);
            return null;
        }

        String targetAttribute = reader.getAttributeValue(null, "source");
        List<URI> sources = new ArrayList<>();
        ConsumerDefinition<ComponentDefinition> consumer = new ConsumerDefinition<>(name);
        try {
            if (targetAttribute != null) {
                StringTokenizer tokenizer = new StringTokenizer(targetAttribute);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (token.startsWith("\\\\")) {
                        // a domain level channel
                        token = "domain://" + token.substring(2);
                    }
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
                    UnrecognizedElement failure = new UnrecognizedElement(reader, location, consumer);
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