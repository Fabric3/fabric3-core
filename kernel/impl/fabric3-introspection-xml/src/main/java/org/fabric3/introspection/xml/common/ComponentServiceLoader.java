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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component service configuration.
 */
public class ComponentServiceLoader extends AbstractExtensibleTypeLoader<ServiceDefinition> {
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName CALLBACK = new QName(SCA_NS, "callback");

    private boolean roundTrip;

    public ComponentServiceLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "requires", "policySets");
    }

    @Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public QName getXMLType() {
        return SERVICE;
    }

    public ServiceDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }
        ServiceDefinition<ComponentDefinition> definition = new ServiceDefinition<>(name);
        if (roundTrip) {
            definition.enableRoundTrip();
        }

        validateAttributes(reader, context, definition);

        boolean callback = false;
        while (true) {
            int i = reader.next();
            switch (i) {
                case XMLStreamConstants.START_ELEMENT:
                    Location location = reader.getLocation();
                    callback = CALLBACK.equals(reader.getName());
                    if (callback) {
                        reader.nextTag();
                    }
                    QName elementName = reader.getName();
                    Object type = registry.load(reader, Object.class, context);

                    if (type instanceof ServiceContract) {
                        definition.setServiceContract((ServiceContract) type);
                    } else if (type instanceof BindingDefinition) {
                        BindingDefinition binding = (BindingDefinition) type;
                        if (callback) {
                            if (binding.getName() == null) {
                                // set the default binding name
                                BindingHelper.configureName(binding, definition.getCallbackBindings(), location, context);
                            }
                            boolean check = BindingHelper.checkDuplicateNames(binding, definition.getCallbackBindings(), location, context);
                            if (check) {
                                definition.addCallbackBinding(binding);
                            }

                        } else {
                            if (binding.getName() == null) {
                                // set the default binding name
                                BindingHelper.configureName(binding, definition.getBindings(), location, context);
                            }
                            boolean check = BindingHelper.checkDuplicateNames(binding, definition.getBindings(), location, context);
                            if (check) {
                                definition.addBinding(binding);
                            }
                        }
                    } else if (type == null) {
                        // error loading, the element, ignore as an error will have been reported
                        LoaderUtil.skipToEndElement(reader);
                        // check if the last element before the end service tag was at fault, in which case return to avoid reading past the service tag
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && reader.getName().getLocalPart().equals("service")) {
                            return definition;
                        } else {
                            break;
                        }
                    } else {
                        context.addError(new UnrecognizedElement(reader, location, definition));
                        continue;
                    }
                    if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                        throw new AssertionError("Loader must position the cursor to the end element");
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (callback) {
                        callback = false;
                        break;
                    }
                    if (!SERVICE.equals(reader.getName())) {
                        continue;
                    }
                    return definition;
            }
        }
    }

}
