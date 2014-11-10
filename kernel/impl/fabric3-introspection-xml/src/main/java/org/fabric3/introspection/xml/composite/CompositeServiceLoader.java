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

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.Source;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a service definition from a composite.
 */
public class CompositeServiceLoader extends AbstractValidatingTypeLoader<CompositeService> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private LoaderRegistry registry;
    private LoaderHelper loaderHelper;
    private boolean roundTrip;

    public CompositeServiceLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        this.registry = registry;
        this.loaderHelper = loaderHelper;
        addAttributes("name", "requires", "promote", "policySets");
    }

    @Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public CompositeService load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Service name not specified", startLocation);
            context.addError(failure);
            return null;
        }
        CompositeService service = new CompositeService(name);

        URI uri = parsePromote(service, reader, startLocation, context);
        service.setPromote(uri);

        if (roundTrip) {
            service.enableRoundTrip();
        }


        loaderHelper.loadPolicySetsAndIntents(service, reader, context);

        validateAttributes(reader, context, service);

        boolean callback = false;
        while (true) {
            int i = reader.next();
            switch (i) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    service.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    if (callback) {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, service.getCallbackBindings(), location, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, service.getCallbackBindings(), location, context);
                        if (check) {
                            service.addCallbackBinding(binding);
                        }
                    } else {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, service.getBindings(), location, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, service.getBindings(), location, context);
                        if (check) {
                            service.addBinding(binding);
                        }
                    }
                } else if (type == null) {
                    // there was an error loading the element, ignore it as the errors will have been reported
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader, location, service));
                    continue;
                }
                if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                    throw new AssertionError("Loader must position the cursor to the end element");
                }
                break;
            case END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return service;
            }
        }
    }

    private URI parsePromote(CompositeService service, XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String name = service.getName();
        String promote = reader.getAttributeValue(null, "promote");
        if (promote == null) {
            MissingPromotion error = new MissingPromotion("Promotion not specified on composite service " + name, startLocation, service);
            context.addError(error);
        }
        URI uri;
        try {
            uri = loaderHelper.parseUri(promote);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid promote URI specified on service " + name, startLocation, e, service);
            context.addError(error);
            uri = URI.create("");
        }
        if (uri == null) {
            InvalidValue error = new InvalidValue("Empty promote URI specified on service " + name, startLocation, service);
            context.addError(error);
            uri = URI.create("");
        }
        return uri;
    }

}
