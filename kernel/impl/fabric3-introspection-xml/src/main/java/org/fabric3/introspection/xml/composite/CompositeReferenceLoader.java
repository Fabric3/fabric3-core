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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a reference from a composite.
 */
public class CompositeReferenceLoader extends AbstractValidatingTypeLoader<CompositeReference> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private LoaderRegistry registry;
    private LoaderHelper loaderHelper;
    private boolean roundTrip;

    public CompositeReferenceLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        this.registry = registry;
        this.loaderHelper = loaderHelper;
        addAttributes("name", "autowire", "promote", "multiplicity", "requires", "policySets");
    }

    @Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public CompositeReference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Reference name not specified", startLocation);
            context.addError(failure);
            return null;
        }

        CompositeReference reference = new CompositeReference(name);
        validateAttributes(reader, context, reference);

        List<URI> promotedUris;
        boolean promoteError = false;
        try {
            promotedUris = loaderHelper.parseListOfUris(reader, "promote");
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid promote URI specified on reference: " + name, startLocation, e, reference);
            context.addError(error);
            promotedUris = Collections.emptyList();
            promoteError = true;
        }
        if (!promoteError && (promotedUris == null || promotedUris.isEmpty())) {
            MissingPromotion error = new MissingPromotion("Promotion not specified on composite reference " + name, startLocation, reference);
            context.addError(error);
        }
        Multiplicity multiplicity = Multiplicity.ONE_ONE;
        String multiplicityValue = reader.getAttributeValue(null, "multiplicity");
        try {
            if (multiplicityValue != null) {
                multiplicity = Multiplicity.fromString(multiplicityValue);
            }
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + multiplicityValue, startLocation, reference);
            context.addError(failure);
        }

        reference.setMultiplicity(multiplicity);
        reference.setPromotedUris(promotedUris);

        if (roundTrip) {
            reference.enableRoundTrip();
            //noinspection VariableNotUsedInsideIf
            if (multiplicityValue != null) {
                reference.attributeSpecified("multiplicity");
            }

        }

        boolean callback = false;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                Location location = reader.getLocation();
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                QName elementName = reader.getName();
                ModelObject type = registry.load(reader, ModelObject.class, context);
                if (type instanceof ServiceContract) {
                    reference.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    if (callback) {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, reference.getCallbackBindings(), location, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, reference.getCallbackBindings(), location, context);
                        if (check) {
                            reference.addCallbackBinding(binding);
                        }
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
                } else if (type == null) {
                    // there was an error loading the element, ignore it as the errors will have been reported
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
            case XMLStreamConstants.END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return reference;
            }
        }

    }
}
