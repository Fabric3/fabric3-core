/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.introspection.xml.composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.oasisopen.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;

/**
 * Loads a component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ComponentLoader extends AbstractExtensibleTypeLoader<ComponentDefinition<?>> {

    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("autowire", "autowire");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
        ATTRIBUTES.put("key", "key");
    }

    private LoaderHelper loaderHelper;
    private ContractMatcher contractMatcher;

    /**
     * Constructor used during boostrap
     *
     * @param registry     the loader registry
     * @param loaderHelper the helper
     */
    public ComponentLoader(LoaderRegistry registry, LoaderHelper loaderHelper) {
        super(registry);
        this.loaderHelper = loaderHelper;
    }

    @Constructor
    public ComponentLoader(@Reference LoaderRegistry registry,
                           @Reference(name = "loaderHelper") LoaderHelper loaderHelper,
                           @Reference ContractMatcher contractMatcher) {
        super(registry);
        this.loaderHelper = loaderHelper;
        this.contractMatcher = contractMatcher;
    }

    public ComponentDefinition<?> load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Component name not specified", reader);
            context.addError(failure);
            return null;
        }

        ComponentDefinition<Implementation<?>> definition = new ComponentDefinition<Implementation<?>>(name);
        Autowire autowire = Autowire.fromString(reader.getAttributeValue(null, "autowire"));
        definition.setAutowire(autowire);

        String key = loaderHelper.loadKey(reader);
        definition.setKey(key);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        Implementation<?> impl;
        try {
            reader.nextTag();
            QName elementName = reader.getName();
            if (COMPONENT.equals(elementName)) {
                // the read er has hit the end of the component definition without an implementation being specified
                MissingComponentImplementation error =
                        new MissingComponentImplementation("The component " + name + " must specify an implementation", reader);
                context.addError(error);
                return definition;
            } else if (PROPERTY.equals(elementName) || REFERENCE.equals(elementName) || SERVICE.equals(elementName)) {
                MissingComponentImplementation error = new MissingComponentImplementation("The component " + name
                        + " must specify an implementation as the first child element", reader);
                context.addError(error);
                return definition;
            }
            impl = registry.load(reader, Implementation.class, context);
            if (impl == null || impl.getComponentType() == null) {
                // error loading impl
                return definition;
            }

            if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                // ensure that the implementation loader has positioned the cursor to the end element 
                throw new AssertionError("Impementation loader must position the cursor to the end element");
            }
            definition.setImplementation(impl);
            AbstractComponentType<?, ?, ?, ?> componentType = impl.getComponentType();

            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (PROPERTY.equals(qname)) {
                        parseProperty(definition, componentType, reader, context);
                    } else if (REFERENCE.equals(qname)) {
                        parseReference(definition, componentType, reader, context);
                    } else if (SERVICE.equals(qname)) {
                        parseService(definition, componentType, reader, context);
                    } else {
                        // Unknown extension element - issue an error and continue
                        context.addError(new UnrecognizedElement(reader));
                        LoaderUtil.skipToEndElement(reader);
                    }
                    break;
                case END_ELEMENT:
                    assert COMPONENT.equals(reader.getName());
                    validateRequiredProperties(definition, reader, context);
                    return definition;
                }
            }
        } catch (UnrecognizedElementException e) {
            UnrecognizedElement failure = new UnrecognizedElement(reader);
            context.addError(failure);
            return null;
        }
    }

    public QName getXMLType() {
        return COMPONENT;
    }

    private void parseService(ComponentDefinition<Implementation<?>> definition,
                              AbstractComponentType<?, ?, ?, ?> componentType,
                              XMLStreamReader reader,
                              IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentService service;
        service = registry.load(reader, ComponentService.class, context);
        if (service == null) {
            // there was an error with the service configuration, just skip it
            return;
        }
        String name = service.getName();
        ServiceDefinition typeService = componentType.getServices().get(name);
        if (typeService == null) {
            // ensure the service exists
            ComponentServiceNotFound failure = new ComponentServiceNotFound(name, definition, reader);
            context.addError(failure);
            return;
        }

        processServiceContract(service, typeService, reader, context);

        if (definition.getServices().containsKey(name)) {
            DuplicateComponentService failure = new DuplicateComponentService(name, definition.getName(), reader);
            context.addError(failure);
        } else {
            definition.add(service);
        }
    }

    private void parseReference(ComponentDefinition<Implementation<?>> definition,
                                AbstractComponentType<?, ?, ?, ?> componentType,
                                XMLStreamReader reader,
                                IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentReference reference;
        reference = registry.load(reader, ComponentReference.class, context);
        if (reference == null) {
            // there was an error with the reference configuration, just skip it
            return;
        }
        String name = reference.getName();
        ReferenceDefinition typeReference = componentType.getReferences().get(name);
        if (typeReference == null) {
            // ensure the reference exists
            ComponentReferenceNotFound failure = new ComponentReferenceNotFound(name, definition, reader);
            context.addError(failure);
            return;
        }

        processReferenceContract(reference, typeReference, reader, context);

        if (definition.getReferences().containsKey(name)) {
            DuplicateComponentReference failure = new DuplicateComponentReference(name, definition.getName(), reader);
            context.addError(failure);
            return;
        }

        processMultiplicity(reference, typeReference, reader, context);

        definition.add(reference);

    }

    private void parseProperty(ComponentDefinition<Implementation<?>> definition,
                               AbstractComponentType<?, ?, ?, ?> componentType,
                               XMLStreamReader reader,
                               IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        PropertyValue value;
        value = registry.load(reader, PropertyValue.class, context);
        if (value == null) {
            // there was an error with the property configuration, just skip it
            return;
        }
        if (componentType.getProperties().get(value.getName()) == null) {
            // ensure the property exists
            ComponentPropertyNotFound failure = new ComponentPropertyNotFound(value.getName(), definition, reader);
            context.addError(failure);
            return;
        }
        if (definition.getPropertyValues().containsKey(value.getName())) {
            String id = value.getName();
            DuplicateConfiguredProperty failure = new DuplicateConfiguredProperty(id, definition, reader);
            context.addError(failure);
        } else {
            definition.add(value);
        }
    }
    
    /**
     * Sets the composite service contract from the comonent type reference if not explicitly configured. If configured, validates the contract
     * matches the component type service contract.
     *
     * @param service     the service
     * @param typeService the component type service
     * @param reader      the reader
     * @param context     the context
     */
    private void processServiceContract(ComponentService service,
                                        ServiceDefinition typeService,
                                        XMLStreamReader reader,
                                        IntrospectionContext context) {
        if (service.getServiceContract() == null) {
            // if the service contract is not set, inherit from the component type service
            service.setServiceContract(typeService.getServiceContract());
        } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
            // verify service contracts are compatible
            MatchResult result = contractMatcher.isAssignableFrom(typeService.getServiceContract(), service.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = service.getName();
                IncompatibleContracts error = new IncompatibleContracts("The component service interface " + name
                        + " is not compatible with the promoted service " + typeService.getName() + ": " + result.getError(), reader);
                context.addError(error);
            }
        }
    }

    /**
     * Sets the composite reference service contract from the comonent type reference if not explicitly configured. If configured, validates the
     * contract matches the promoted reference contract.
     *
     * @param reference     the reference
     * @param typeReference the component type reference
     * @param reader        the reader
     * @param context       the context
     */
    private void processReferenceContract(ComponentReference reference,
                                          ReferenceDefinition typeReference,
                                          XMLStreamReader reader,
                                          IntrospectionContext context) {
        if (reference.getServiceContract() == null) {
            // if the reference contract is not set, inherit from the component type service
            reference.setServiceContract(typeReference.getServiceContract());
        } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
            // verify service contracts are compatible
            MatchResult result = contractMatcher.isAssignableFrom(typeReference.getServiceContract(), reference.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = reference.getName();
                IncompatibleContracts error = new IncompatibleContracts("The component reference interface " + name
                        + " is not compatible with the promoted reference " + typeReference.getName() + ": " + result.getError(), reader);
                context.addError(error);
            }
        }
    }

    /**
     * Sets the composite multiplicity to inherit from the component type reference if not explicitly configured. If configured, validates the
     * setting against the component type setting.
     *
     * @param reference     the reference
     * @param typeReference the promoted reference
     * @param reader        the reader
     * @param context       the context
     */
    private void processMultiplicity(ComponentReference reference,
                                     ReferenceDefinition typeReference,
                                     XMLStreamReader reader,
                                     IntrospectionContext context) {
        String name = reference.getName();
        if (reference.getMultiplicity() == null) {
            Multiplicity multiplicity = typeReference.getMultiplicity();
            reference.setMultiplicity(multiplicity);
        } else {
            if (!loaderHelper.canNarrow(reference.getMultiplicity(), typeReference.getMultiplicity())) {
                InvalidValue failure = new InvalidValue("The multiplicity setting for reference " + name + " widens the default setting", reader);
                context.addError(failure);
            }
        }
        List<Target> targets = reference.getTargets();
        Multiplicity multiplicity = reference.getMultiplicity();
        if (targets.size() > 1 && (Multiplicity.ZERO_ONE == multiplicity || Multiplicity.ONE_ONE == multiplicity)) {
            InvalidValue failure = new InvalidValue("Multiple targets configured on reference " + name + ", which takes a single target", reader);
            context.addError(failure);
        }
    }

    private void validateRequiredProperties(ComponentDefinition<? extends Implementation<?>> definition,
                                            XMLStreamReader reader,
                                            IntrospectionContext context) {
        AbstractComponentType<?, ?, ?, ?> type = definition.getImplementation().getComponentType();
        Map<String, ? extends Property> properties = type.getProperties();
        Map<String, PropertyValue> values = definition.getPropertyValues();
        for (Property property : properties.values()) {
            if (property.isRequired() && !values.containsKey(property.getName())) {
                RequiredPropertyNotProvided failure = new RequiredPropertyNotProvided(property, definition.getName(), reader);
                context.addError(failure);
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}

