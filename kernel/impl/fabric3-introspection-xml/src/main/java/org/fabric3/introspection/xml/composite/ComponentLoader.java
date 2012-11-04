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
package org.fabric3.introspection.xml.composite;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.introspection.xml.common.InvalidAttributes;
import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentConsumer;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentProducer;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyMany;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.Target;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.IncompatibleContracts;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a component definition from an XML-based assembly file
 */
@EagerInit
public class ComponentLoader extends AbstractExtensibleTypeLoader<ComponentDefinition<?>> {

    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final QName PRODUCER = new QName(SCA_NS, "producer");
    private static final QName CONSUMER = new QName(SCA_NS, "consumer");

    private LoaderHelper loaderHelper;
    private ContractMatcher contractMatcher;
    private boolean roundTrip;

    /**
     * Constructor used during bootstrap
     *
     * @param registry     the loader registry
     * @param loaderHelper the helper
     */
    public ComponentLoader(LoaderRegistry registry, LoaderHelper loaderHelper) {
        this(registry, loaderHelper, null);
    }

    @Constructor
    public ComponentLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper, @Reference ContractMatcher contractMatcher) {
        super(registry);
        addAttributes("name", "autowire", "requires", "policySets", "key");
        this.loaderHelper = loaderHelper;
        this.contractMatcher = contractMatcher;
    }

    @org.oasisopen.sca.annotation.Property(required = false)
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    public ComponentDefinition<?> load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Component name not specified", reader);
            context.addError(failure);
            return null;
        }
        String autowireStr = reader.getAttributeValue(null, "autowire");
        Autowire autowire = Autowire.fromString(autowireStr);
        String key = loaderHelper.loadKey(reader);

        ComponentDefinition<Implementation<?>> definition = new ComponentDefinition<Implementation<?>>(name);
        if (roundTrip) {
            definition.enableRoundTrip();
            if (autowireStr != null) {
                definition.attributeSpecified("autowire");
            }
            if (key != null) {
                definition.attributeSpecified("key");
            }
        }
        definition.setContributionUri(context.getContributionUri());
        definition.setAutowire(autowire);
        definition.setKey(key);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        Implementation<?> impl;
        try {
            if (roundTrip) {
                LoaderUtil.nextTagRecord(definition, reader);
            } else {
                reader.nextTag();
            }
            QName elementName = reader.getName();
            if (COMPONENT.equals(elementName)) {
                // the reader has hit the end of the component definition without an implementation being specified
                MissingComponentImplementation error =
                        new MissingComponentImplementation("The component " + name + " must specify an implementation", reader);
                context.addError(error);
                return definition;
            } else if (PROPERTY.equals(elementName) || REFERENCE.equals(elementName) || SERVICE.equals(elementName) || PRODUCER.equals(elementName)) {
                MissingComponentImplementation error = new MissingComponentImplementation("The component " + name
                                                                                                  + " must specify an implementation as the first child element",
                                                                                          reader);
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
                throw new AssertionError("Implementation loader must position the cursor to the end element");
            }
            definition.setImplementation(impl);
            ComponentType componentType = impl.getComponentType();

            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (PROPERTY.equals(qname)) {
                        parsePropertyValue(definition, componentType, reader, context);
                    } else if (REFERENCE.equals(qname)) {
                        parseReference(definition, componentType, reader, context);
                    } else if (SERVICE.equals(qname)) {
                        parseService(definition, componentType, reader, context);
                    } else if (PRODUCER.equals(qname)) {
                        parseProducer(definition, componentType, reader, context);
                    } else if (CONSUMER.equals(qname)) {
                        parseConsumer(definition, componentType, reader, context);
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
                case XMLStreamReader.COMMENT:
                    if (!roundTrip) {
                        continue;
                    }
                    String comment = reader.getText();
                    definition.addComment(comment);
                    continue;
                default:
                    if (!roundTrip) {
                        continue;
                    }
                    comment = reader.getText();
                    definition.addText(comment);
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

    private void parseService(ComponentDefinition<?> definition,
                              ComponentType componentType,
                              XMLStreamReader reader,
                              IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentService service = registry.load(reader, ComponentService.class, context);
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

    private void parseReference(ComponentDefinition<?> definition,
                                ComponentType componentType,
                                XMLStreamReader reader,
                                IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentReference reference = registry.load(reader, ComponentReference.class, context);
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

        if (!reference.getCallbackBindings().isEmpty()) {
            if (typeReference != null) {
                if (typeReference.getServiceContract() != null && typeReference.getServiceContract().getCallbackContract() == null) {
                    InvalidServiceContract failure = new InvalidServiceContract(
                            "Reference is configured with a callback binding but its service contract is not bidirectional: " + name,
                            reader);
                    context.addError(failure);
                }

            }
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

    private void parseProducer(ComponentDefinition<Implementation<?>> definition,
                               ComponentType componentType,
                               XMLStreamReader reader,
                               IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentProducer producer = registry.load(reader, ComponentProducer.class, context);
        if (producer == null) {
            // there was an error with the producer configuration, just skip it
            return;
        }
        String name = producer.getName();
        ProducerDefinition typeProducer = componentType.getProducers().get(name);
        if (typeProducer == null) {
            // ensure the producer exists
            ComponentProducerNotFound failure = new ComponentProducerNotFound(name, definition, reader);
            context.addError(failure);
            return;
        }
        definition.add(producer);
    }

    private void parseConsumer(ComponentDefinition<Implementation<?>> definition,
                               ComponentType componentType,
                               XMLStreamReader reader,
                               IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        ComponentConsumer consumer = registry.load(reader, ComponentConsumer.class, context);
        if (consumer == null) {
            // there was an error with the consumer configuration, just skip it
            return;
        }
        String name = consumer.getName();
        ConsumerDefinition typeConsumer = componentType.getConsumers().get(name);
        if (typeConsumer == null) {
            // ensure the consumer exists
            ComponentConsumerNotFound failure = new ComponentConsumerNotFound(name, definition, reader);
            context.addError(failure);
            return;
        }
        consumer.setTypes(typeConsumer.getTypes());
        definition.add(consumer);
    }


    private void parsePropertyValue(ComponentDefinition<?> definition,
                                    ComponentType componentType,
                                    XMLStreamReader reader,
                                    IntrospectionContext context) throws XMLStreamException, UnrecognizedElementException {
        PropertyValue value = registry.load(reader, PropertyValue.class, context);
        if (value == null) {
            // there was an error with the property configuration, just skip it
            return;
        }
        String name = value.getName();
        Property property = componentType.getProperties().get(name);
        if (property == null) {
            // ensure the property exists
            ComponentPropertyNotFound failure = new ComponentPropertyNotFound(value.getName(), definition, reader);
            context.addError(failure);
            return;
        }
        validatePropertyType(value, property, reader, context);
        if (definition.getPropertyValues().containsKey(value.getName())) {
            String id = value.getName();
            DuplicateConfiguredProperty failure = new DuplicateConfiguredProperty(id, definition, reader);
            context.addError(failure);
        } else {
            definition.add(value);
        }
        if (value.getValue() != null && value.getValue().getDocumentElement().getChildNodes().getLength() == 0 && property.isRequired()) {
            // property value not specified
            PropertyValueNotSpecified failure = new PropertyValueNotSpecified(value.getName(), definition, reader);
            context.addError(failure);
        }
    }

    /**
     * Sets the composite service contract from the component type reference if not explicitly configured. If configured, validates the contract
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
            // verify service contracts are compatible - the component service contract can be a subset of the component type service contract
            MatchResult result = contractMatcher.isAssignableFrom(service.getServiceContract(), typeService.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = service.getName();
                IncompatibleContracts error = new IncompatibleContracts("The component service interface " + name
                                                                                + " is not compatible with the promoted service " + typeService.getName() + ": " + result.getError(),
                                                                        reader);
                context.addError(error);
            } else {
                matchServiceCallbackContracts(service, typeService, reader, context);
            }
        }
    }

    /**
     * Sets the composite reference service contract from the component type reference if not explicitly configured. If configured, validates the
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
            // verify service contracts are compatible - the component type reference contract can be a subset of the component reference contract
            MatchResult result = contractMatcher.isAssignableFrom(typeReference.getServiceContract(), reference.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = reference.getName();
                IncompatibleContracts error = new IncompatibleContracts("The component reference contract " + name
                                                                                + " is not compatible with the promoted reference " + typeReference.getName() + ": " + result.getError(),
                                                                        reader);
                context.addError(error);
            } else {
                matchReferenceCallbackContracts(reference, typeReference, reader, context);
            }
        }
    }

    /**
     * Matches the service contract declared on the promoted service and component type service.
     *
     * @param service     the service
     * @param typeService the component type service
     * @param reader      the reader
     * @param context     the context
     */
    private void matchServiceCallbackContracts(ComponentService service,
                                               ServiceDefinition typeService,
                                               XMLStreamReader reader,
                                               IntrospectionContext context) {
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract typeCallbackContract = typeService.getServiceContract().getCallbackContract();
        if (typeCallbackContract == null) {
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for service " + service.getName() + " does not have a callback contract", reader);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(typeCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            String name = service.getName();
            IncompatibleContracts error = new IncompatibleContracts("The component service " + name + " callback contract is not compatible with " +
                                                                            "the promoted service " + typeService.getName() + " callback contract: " + result.getError(),
                                                                    reader);
            context.addError(error);
        }
    }

    /**
     * Matches the service contract declared on the promoted reference and component type reference.
     *
     * @param reference     the reference
     * @param typeReference the component type reference
     * @param reader        the reader
     * @param context       the context
     */
    private void matchReferenceCallbackContracts(ComponentReference reference,
                                                 ReferenceDefinition typeReference,
                                                 XMLStreamReader reader,
                                                 IntrospectionContext context) {
        ServiceContract callbackContract = reference.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract typeCallbackContract = typeReference.getServiceContract().getCallbackContract();
        if (typeCallbackContract == null) {
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for reference " + reference.getName() + " does not have a callback contract", reader);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(typeCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            String name = reference.getName();
            IncompatibleContracts error = new IncompatibleContracts("The component reference " + name + " callback contract is not compatible with " +
                                                                            "the promoted reference " + typeReference.getName() + " callback contract: " + result.getError(),
                                                                    reader);
            context.addError(error);
        }
    }


    /**
     * Sets the composite multiplicity to inherit from the component type reference if not explicitly configured. If configured, validates the setting
     * against the component type setting.
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

    private void validateRequiredProperties(ComponentDefinition<?> definition, XMLStreamReader reader, IntrospectionContext context) {
        ComponentType type = definition.getImplementation().getComponentType();
        Map<String, ? extends Property> properties = type.getProperties();
        Map<String, PropertyValue> values = definition.getPropertyValues();
        for (Property property : properties.values()) {
            PropertyValue value = values.get(property.getName());
            if (property.isRequired() && value == null) {
                RequiredPropertyNotProvided failure = new RequiredPropertyNotProvided(property, definition.getName(), reader);
                context.addError(failure);
                continue;
            }
            if (value != null) {
                // null check since an optional property may not be configured on the component
                validateAndSetMany(value, property, reader, context);
            }
        }
    }

    private void validateAndSetMany(PropertyValue propertyValue, Property property, XMLStreamReader reader, IntrospectionContext context) {
        PropertyMany propertyMany = propertyValue.getMany();
        if (PropertyMany.NOT_SPECIFIED == propertyMany) {
            if (property.isMany()) {
                propertyValue.setMany(PropertyMany.MANY);
            } else {
                propertyValue.setMany(PropertyMany.SINGLE);
            }
        } else if (PropertyMany.MANY == propertyMany) {
            if (!property.isMany()) {
                InvalidPropertyConfiguration error = new InvalidPropertyConfiguration("Illegal attempt to make a property many-valued when its " +
                                                                                              "component type is single-valued", reader);
                context.addError(error);
                return;

            }
            propertyValue.setMany(PropertyMany.MANY);
        } else {
            propertyValue.setMany(PropertyMany.SINGLE);
        }
        Document value = propertyValue.getValue();
        if (value != null && PropertyMany.MANY != propertyValue.getMany() && value.getDocumentElement().getChildNodes().getLength() > 1) {
            // null check since optional properties may have null values
            // validate the many
            String name = propertyValue.getName();
            InvalidPropertyValue error = new InvalidPropertyValue("A single-valued property is configured with multiple values: " + name, reader);
            context.addError(error);
        }
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    private void validatePropertyType(PropertyValue value, Property property, XMLStreamReader reader, IntrospectionContext context) {
        QName propType = property.getType();
        QName propElement = property.getElement();
        QName valType = value.getType();
        QName valElement = value.getElement();
        if (propType != null) {
            if (valElement != null) {
                InvalidAttributes error = new InvalidAttributes("Cannot specify property schema type and element type on property configuration: "
                                                                        + value.getName(), reader);
                context.addError(error);
            } else if (valType != null && !valType.equals(propType)) {
                InvalidAttributes error = new InvalidAttributes("Property type " + propType + " and property configuration type " + valType
                                                                        + " do not match: " + value.getName(), reader);
                context.addError(error);
            }
        } else if (propElement != null) {
            if (valType != null) {
                InvalidAttributes error = new InvalidAttributes("Cannot specify property element type and property configuration schema type: "
                                                                        + value.getName(), reader);
                context.addError(error);
            } else if (valElement != null && !valElement.equals(propElement)) {
                InvalidAttributes error = new InvalidAttributes("Property element type " + propElement + " and property configuration element type "
                                                                        + valElement + " do not match: " + value.getName(), reader);
                context.addError(error);
            }
        }

    }


}

