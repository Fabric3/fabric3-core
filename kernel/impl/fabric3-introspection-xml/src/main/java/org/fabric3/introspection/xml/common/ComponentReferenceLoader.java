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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component reference configuration.
 */
public class ComponentReferenceLoader extends AbstractExtensibleTypeLoader<Reference> {
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final QName CALLBACK = new QName(SCA_NS, "callback");

    private LoaderHelper loaderHelper;

    public ComponentReferenceLoader(@org.oasisopen.sca.annotation.Reference LoaderRegistry registry, @org.oasisopen.sca.annotation.Reference LoaderHelper loaderHelper) {
        super(registry);
        addAttributes("name", "autowire", "target", "multiplicity", "requires", "policySets", "nonOverridable");
        this.loaderHelper = loaderHelper;
    }

    public QName getXMLType() {
        return REFERENCE;
    }

    public Reference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingReferenceName failure = new MissingReferenceName(startLocation);
            context.addError(failure);
            return null;
        }

        Multiplicity multiplicity = parseMultiplicity(reader, startLocation, context);

        Reference<Component> reference = new Reference<>(name, multiplicity);


        parseTargets(reference, reader, startLocation, context);

        validateAttributes(reader, context, reference);

        boolean callback = false;
        boolean bindingError = false;  // used to avoid reporting multiple binding errors
        while (true) {

            switch (reader.next()) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    reference.setServiceContract((ServiceContract) type);
                } else if (type instanceof Binding) {
                    Binding binding = (Binding) type;
                    if (!reference.getTargets().isEmpty()) {
                        if (!bindingError) {
                            // bindings cannot be configured on references if the @target attribute is used
                            InvalidBinding error =
                                    new InvalidBinding("Bindings cannot be configured when the target attribute on a reference is used: "
                                                               + name, location, binding);
                            context.addError(error);
                            bindingError = true;
                        }
                        continue;
                    }
                    configureBinding(reference, binding, callback, location, context);
                } else if (type == null) {
                    // no type, continue processing
                    continue;
                } else {
                    UnrecognizedElement failure = new UnrecognizedElement(reader, location, reference);
                    context.addError(failure);
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
                if (!REFERENCE.equals(reader.getName())) {
                    continue;
                }
                return reference;
            }
        }
    }

    private Multiplicity parseMultiplicity(XMLStreamReader reader, Location location, IntrospectionContext context) {
        String value = reader.getAttributeValue(null, "multiplicity");
        Multiplicity multiplicity = null;
        try {
            if (value != null) {
                multiplicity = Multiplicity.fromString(value);
            }
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + value, location);
            context.addError(failure);
        }
        return multiplicity;
    }

    private void parseTargets(Reference reference, XMLStreamReader reader, Location location, IntrospectionContext context) {
        String targetAttribute = reader.getAttributeValue(null, "target");
        List<Target> targets = new ArrayList<>();
        try {
            if (targetAttribute != null) {
                StringTokenizer tokenizer = new StringTokenizer(targetAttribute);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    Target target = loaderHelper.parseTarget(token, reader);
                    targets.add(target);
                }
            }
        } catch (InvalidTargetException e) {
            InvalidValue failure = new InvalidValue("Invalid target format", location, e);
            context.addError(failure);
        }
        reference.addTargets(targets);
    }

    private void configureBinding(Reference<Component> reference,
                                  Binding binding,
                                  boolean callback,
                                  Location location,
                                  IntrospectionContext context) {
        if (callback) {
            if (binding.getName() == null) {
                // set the default binding name
                BindingHelper.configureName(binding, reference.getCallbackBindings(), location, context);
            }
            reference.addCallbackBinding(binding);
        } else {
            if (binding.getName() == null) {
                // set the default binding name
                BindingHelper.configureName(binding, reference.getBindings(), location, context);
            }

            boolean check = BindingHelper.checkDuplicateNames(binding, reference.getBindings(), location, context);
            if (check) {
                reference.addBinding(binding);
            }
        }
    }

}
