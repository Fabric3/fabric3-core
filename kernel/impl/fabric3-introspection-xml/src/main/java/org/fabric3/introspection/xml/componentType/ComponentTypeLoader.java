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
package org.fabric3.introspection.xml.componentType;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.introspection.xml.common.MissingReferenceName;
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a generic component type.
 */
@EagerInit
public class ComponentTypeLoader implements TypeLoader<ComponentType> {
    private static final QName COMPONENT_TYPE = new QName(SCA_NS, "componentType");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final QName CALLBACK = new QName(SCA_NS, "callback");

    private LoaderHelper loaderHelper;

    private final LoaderRegistry registry;
    private final TypeLoader<Property> propertyLoader;

    public ComponentTypeLoader(@Reference LoaderRegistry registry,
                               @Reference LoaderHelper loaderHelper,
                               @Reference(name = "property") TypeLoader<Property> propertyLoader) {
        this.registry = registry;
        this.loaderHelper = loaderHelper;
        this.propertyLoader = propertyLoader;
    }

    @Init
    public void init() {
        registry.registerLoader(COMPONENT_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(COMPONENT_TYPE);
    }

    public QName getXMLType() {
        return COMPONENT_TYPE;
    }

    public ComponentType load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        ComponentType type = new ComponentType();
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                Location location = reader.getLocation();
                QName qname = reader.getName();
                if (PROPERTY.equals(qname)) {
                    Property property = propertyLoader.load(reader, introspectionContext);
                    type.add(property);
                } else if (SERVICE.equals(qname)) {
                    ServiceDefinition service;
                    service = loadService(reader, introspectionContext);
                    type.add(service);
                } else if (REFERENCE.equals(qname)) {
                    ReferenceDefinition reference = loadReference(reader, introspectionContext);
                    type.add(reference);
                } else {
                    // Extension element - for now try to load and see if we can handle it
                    ModelObject modelObject = registry.load(reader, ModelObject.class, introspectionContext);
                    if (modelObject instanceof Property) {
                        type.add((Property) modelObject);
                    } else if (modelObject instanceof ServiceDefinition) {
                        type.add((ServiceDefinition) modelObject);
                    } else if (modelObject instanceof ReferenceDefinition) {
                        type.add((ReferenceDefinition) modelObject);
                    } else if (modelObject != null) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader, location, type);
                        introspectionContext.addError(failure);
                        continue;
                    }
                }
                break;
            case END_ELEMENT:
                return type;
            }
        }
    }

    private ServiceDefinition loadService(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateServiceAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }
        ServiceDefinition service = new ServiceDefinition(name, null);

        loaderHelper.loadPolicySetsAndIntents(service, reader, context);

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
                    // error loading, the element, ignore as an error will have been reported
                    continue;
                } else {
                    UnrecognizedElement failure = new UnrecognizedElement(reader, location, service);
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
                return service;
            }
        }
    }

    private ReferenceDefinition loadReference(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateReferenceAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingReferenceName failure = new MissingReferenceName(startLocation);
            context.addError(failure);
            return null;
        }

        String value = reader.getAttributeValue(null, "multiplicity");
        Multiplicity multiplicity = Multiplicity.ONE_ONE;   // for component types, default 1..1
        try {
            if (value != null) {
                multiplicity = Multiplicity.fromString(value);
            }
        } catch (IllegalArgumentException e) {
            InvalidValue failure = new InvalidValue("Invalid multiplicity value: " + value, startLocation);
            context.addError(failure);
        }
        ReferenceDefinition reference = new ReferenceDefinition(name, multiplicity);

        loaderHelper.loadPolicySetsAndIntents(reference, reader, context);

        boolean callback = false;
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
                // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
                // UnrecognizedElement added to the context if none is found
                if (type instanceof ServiceContract) {
                    reference.setServiceContract((ServiceContract) type);
                } else if (type instanceof BindingDefinition) {
                    BindingDefinition binding = (BindingDefinition) type;
                    if (callback) {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, reference.getCallbackBindings(), startLocation, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, reference.getCallbackBindings(), startLocation, context);
                        if (check) {
                            reference.addCallbackBinding(binding);
                        }
                    } else {
                        if (binding.getName() == null) {
                            // set the default binding name
                            BindingHelper.configureName(binding, reference.getBindings(), startLocation, context);
                        }
                        boolean check = BindingHelper.checkDuplicateNames(binding, reference.getBindings(), startLocation, context);
                        if (check) {
                            reference.addBinding(binding);
                        }
                    }
                } else if (type == null) {
                    // no type, continue processing
                    continue;
                } else {
                    context.addError(new UnrecognizedElement(reader, location, reference));
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
                return reference;
            }
        }
    }

    private void validateReferenceAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"requires".equals(name) && !"policySets".equals(name) && !"multiplicity".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, location));
            }
        }
    }

    private void validateServiceAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"requires".equals(name) && !"policySets".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, location));
            }
        }
    }


}
