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
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component producer configuration.
 */
public class ComponentProducerLoader extends AbstractExtensibleTypeLoader<Producer<Component>> {
    private static final QName PRODUCER = new QName(SCA_NS, "producer");

    public ComponentProducerLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "target");
    }

    public QName getXMLType() {
        return PRODUCER;
    }

    public Producer<Component> load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingProducerName failure = new MissingProducerName(startLocation);
            context.addError(failure);
            return null;
        }

        String targetAttribute = reader.getAttributeValue(null, "target");
        Producer<Component> producer = new Producer<>(name);

        List<URI> targets = new ArrayList<>();
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

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    producer.setServiceContract((ServiceContract) type);
                } else if (type instanceof Binding) {
                    Binding binding = (Binding) type;
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