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

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.oasisopen.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.IncompatibleContracts;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;
import org.fabric3.spi.util.UriHelper;

/**
 * Loads a composite component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeLoader extends AbstractExtensibleTypeLoader<Composite> {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    public static final QName INCLUDE = new QName(SCA_NS, "include");
    public static final QName CHANNEL = new QName(SCA_NS, "channel");
    public static final QName PROPERTY = new QName(SCA_NS, "property");
    public static final QName SERVICE = new QName(SCA_NS, "service");
    public static final QName REFERENCE = new QName(SCA_NS, "reference");
    public static final QName COMPONENT = new QName(SCA_NS, "component");
    public static final QName WIRE = new QName(SCA_NS, "wire");

    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("autowire", "autowire");
        ATTRIBUTES.put("targetNamespace", "targetNamespace");
        ATTRIBUTES.put("local", "local");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
        ATTRIBUTES.put("constrainingType", "constrainingType");
        ATTRIBUTES.put("channel", "channel");
    }

    private TypeLoader<CompositeService> serviceLoader;
    private TypeLoader<CompositeReference> referenceLoader;
    private TypeLoader<Property> propertyLoader;
    private ContractMatcher contractMatcher;
    private final LoaderHelper loaderHelper;


    /**
     * Constructor used during bootstrap; service and reference elements are not supported.
     *
     * @param registry       the loader registry to register with; also used to load extension elements
     * @param propertyLoader the property loader
     * @param loaderHelper   helper the helper
     */
    public CompositeLoader(LoaderRegistry registry, TypeLoader<Property> propertyLoader, LoaderHelper loaderHelper) {
        super(registry);
        this.propertyLoader = propertyLoader;
        this.loaderHelper = loaderHelper;
    }

    /**
     * Constructor. Specific loaders to handle overloaded <code>property>, <code>service</code> and <code>reference</code> elements on composites and
     * components.
     *
     * @param registry        the loader registry to register with; also used to load extension elements
     * @param serviceLoader   the service loader
     * @param referenceLoader the reference loader
     * @param propertyLoader  the property loader
     * @param contractMatcher the contract matcher
     * @param loaderHelper    helper the helper
     */
    @Constructor
    public CompositeLoader(@Reference LoaderRegistry registry,
                           @Reference(name = "service") TypeLoader<CompositeService> serviceLoader,
                           @Reference(name = "reference") TypeLoader<CompositeReference> referenceLoader,
                           @Reference(name = "property") TypeLoader<Property> propertyLoader,
                           @Reference ContractMatcher contractMatcher,
                           @Reference LoaderHelper loaderHelper) {
        super(registry);
        this.serviceLoader = serviceLoader;
        this.referenceLoader = referenceLoader;
        this.propertyLoader = propertyLoader;
        this.contractMatcher = contractMatcher;
        this.loaderHelper = loaderHelper;
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    public Composite load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        boolean local = Boolean.valueOf(reader.getAttributeValue(null, "local"));
        IntrospectionContext childContext = new DefaultIntrospectionContext(context, targetNamespace);
        QName compositeName = new QName(targetNamespace, name);

        NamespaceContext nsContext = createNamespaceContext(reader);

        Composite type = new Composite(compositeName);
        type.setContributionUri(context.getContributionUri());
        type.setLocal(local);
        type.setAutowire(Autowire.fromString(reader.getAttributeValue(null, "autowire")));
        loaderHelper.loadPolicySetsAndIntents(type, reader, childContext);
        try {
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (INCLUDE.equals(qname)) {
                        handleInclude(type, reader, childContext);
                        continue;
                    } else if (PROPERTY.equals(qname)) {
                        handleProperty(type, reader, childContext);
                        continue;
                    } else if (SERVICE.equals(qname)) {
                        handleService(type, reader, childContext);
                        continue;
                    } else if (CHANNEL.equals(qname)) {
                        handleChannel(type, reader, childContext);
                        continue;
                    } else if (REFERENCE.equals(qname)) {
                        handleReference(type, reader, childContext);
                        continue;
                    } else if (COMPONENT.equals(qname)) {
                        boolean valid = handleComponent(type, reader, nsContext, childContext);
                        if (!valid) {
                            updateContext(context, childContext, compositeName);
                            return type;
                        }
                        continue;
                    } else if (WIRE.equals(qname)) {
                        handleWire(type, reader, childContext);
                        continue;
                    } else {
                        handleExtensionElement(type, reader, childContext);
                        continue;
                    }
                case END_ELEMENT:
                    assert COMPOSITE.equals(reader.getName());
                    updateAndValidateServicePromotions(type, reader, childContext);
                    updateAndValidateReferencePromotions(type, reader, childContext);
                    updateContext(context, childContext, compositeName);
                    return type;
                }
            }
        } catch (UnrecognizedElementException e) {
            updateContext(context, childContext, compositeName);
            UnrecognizedElement failure = new UnrecognizedElement(reader);
            context.addError(failure);
            return type;
        }
    }

    private void handleExtensionElement(Composite type, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        // Extension element - for now try to load and see if we can handle it
        ModelObject modelObject;
        try {
            modelObject = registry.load(reader, ModelObject.class, context);
            // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
            // UnrecognizedElement added to the context if none is found
        } catch (UnrecognizedElementException e) {
            UnrecognizedElement failure = new UnrecognizedElement(reader);
            context.addError(failure);
            return;
        }
        if (modelObject instanceof Property) {
            type.add((Property) modelObject);
        } else if (modelObject instanceof CompositeService) {
            type.add((CompositeService) modelObject);
        } else if (modelObject instanceof CompositeReference) {
            type.add((CompositeReference) modelObject);
        } else if (modelObject instanceof ComponentDefinition) {
            type.add((ComponentDefinition<?>) modelObject);
        } else if (type == null) {
            // there was an error loading the element, ingore it as the errors will have been reported
        } else {
            context.addError(new UnrecognizedElement(reader));
        }
    }

    private void handleWire(Composite type, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, UnrecognizedElementException {
        WireDefinition wire = registry.load(reader, WireDefinition.class, context);
        if (wire == null) {
            // errror encountered loading the wire
            return;
        }
        type.add(wire);
    }

    private boolean handleComponent(Composite type, XMLStreamReader reader, NamespaceContext nsContext, IntrospectionContext context)
            throws XMLStreamException, UnrecognizedElementException {
        ComponentDefinition<?> componentDefinition = registry.load(reader, ComponentDefinition.class, context);
        if (componentDefinition == null) {
            // errror encountered loading the componentDefinition
            return false;
        }
        String key = componentDefinition.getName();
        if (type.getComponents().containsKey(key)) {
            DuplicateComponentName failure = new DuplicateComponentName(key, reader);
            context.addError(failure);
            return false;
        }
        Implementation<?> implementation = componentDefinition.getImplementation();
        if (implementation == null || implementation.getComponentType() == null) {
            return false;
        }
        if (type.getAutowire() != Autowire.INHERITED && componentDefinition.getAutowire() == Autowire.INHERITED) {
            componentDefinition.setAutowire(type.getAutowire());
        }

        // Calculate the namespace context from the composite element since XMLStreamReader.getNamespaceCount() only returns the number of namespaces
        // declared on the current element. This means namespaces defined on parent elements which are active (e.g. <comoposite>) or not reported.
        // Scoping results in no namespaces being reported 
        for (PropertyValue value : componentDefinition.getPropertyValues().values()) {
            value.setNamespaceContext(nsContext);
        }
        type.add(componentDefinition);
        return true;
    }

    private void handleChannel(Composite type, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, UnrecognizedElementException {
        ChannelDefinition channelDefinition = registry.load(reader, ChannelDefinition.class, context);
        if (channelDefinition == null) {
            // errror encountered loading the channel definition
            return;
        }
        String key = channelDefinition.getName();
        if (type.getChannels().containsKey(key)) {
            DuplicateChannelName failure = new DuplicateChannelName(key, reader);
            context.addError(failure);
            return;
        }
        type.add(channelDefinition);
    }

    private void handleReference(Composite type, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException {
        CompositeReference reference = referenceLoader.load(reader, context);
        if (reference == null) {
            // errror encountered loading the reference
            return;
        }
        if (type.getReferences().containsKey(reference.getName())) {
            String key = reference.getName();
            DuplicatePromotedReference failure = new DuplicatePromotedReference(key, reader);
            context.addError(failure);
        } else {
            type.add(reference);
        }
    }

    private void handleService(Composite type, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        CompositeService service = serviceLoader.load(reader, context);
        if (service == null) {
            // errror encountered loading the service
            return;
        }
        if (type.getServices().containsKey(service.getName())) {
            String key = service.getName();
            DuplicatePromotedService failure = new DuplicatePromotedService(key, reader);
            context.addError(failure);
        } else {
            type.add(service);
        }
    }

    private void handleProperty(Composite type, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, UnrecognizedElementException {
        Property property = propertyLoader.load(reader, context);
        if (property == null) {
            // errror encountered loading the property
            return;
        }
        String key = property.getName();
        if (type.getProperties().containsKey(key)) {
            DuplicateProperty failure = new DuplicateProperty(key, reader);
            context.addError(failure);
        } else {
            type.add(property);
        }
    }

    private void handleInclude(Composite type, XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, UnrecognizedElementException {
        Include include = registry.load(reader, Include.class, context);
        if (include == null) {
            // errror encountered loading the include
            return;
        }
        QName includeName = include.getName();
        if (type.getIncludes().containsKey(includeName)) {
            String identifier = includeName.toString();
            DuplicateInclude failure = new DuplicateInclude(identifier, reader);
            context.addError(failure);
            return;
        }
        Composite included = include.getIncluded();
        if (type.isLocal() != included.isLocal()) {
            InvalidInclude error = new InvalidInclude("Composite " + type.getName() + " has a local value of " + type.isLocal()
                    + " and the included composite " + includeName + " has a value of " + included.isLocal(), reader);
            context.addError(error);
        }
        for (ComponentDefinition definition : included.getComponents().values()) {
            String key = definition.getName();
            if (type.getComponents().containsKey(key)) {
                DuplicateComponentName failure = new DuplicateComponentName(key, reader);
                context.addError(failure);
            }
        }
        type.add(include);
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    private void updateAndValidateServicePromotions(Composite type, XMLStreamReader reader, IntrospectionContext context) {
        for (ServiceDefinition definition : type.getServices().values()) {
            CompositeService service = (CompositeService) definition;
            URI promotedUri = service.getPromote();
            String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
            ComponentDefinition promotedComponent = type.getComponents().get(componentName);
            ServiceDefinition promotedService;
            String name = service.getName();
            if (promotedComponent == null) {
                PromotionNotFound error =
                        new PromotionNotFound("Component " + componentName + " referenced by " + name + " not found", reader);
                context.addError(error);
                continue;
            } else {
                String serviceName = promotedUri.getFragment();
                AbstractComponentType componentType = promotedComponent.getComponentType();
                if (serviceName != null) {
                    promotedService = componentType.getServices().get(serviceName);
                    if (promotedService == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Service " + serviceName + " promoted by " + name + " not found", reader);
                        context.addError(error);
                        continue;
                    }
                } else {
                    Map<String, ServiceDefinition> services = componentType.getServices();
                    int numberOfServices = services.size();
                    if (numberOfServices == 2) {
                        Iterator<? extends ServiceDefinition> iter = services.values().iterator();
                        ServiceDefinition one = iter.next();
                        ServiceDefinition two = iter.next();
                        if (!one.isManagement() && !two.isManagement()) {
                            PromotionNotFound error =
                                    new PromotionNotFound("A promoted service must be specified for " + name, reader);
                            context.addError(error);
                            return;
                        }
                        promotedService = one.isManagement() ? two : one;

                    } else if (numberOfServices == 1) {
                        promotedService = services.values().iterator().next();
                    } else if (numberOfServices == 0) {
                        PromotionNotFound error =
                                new PromotionNotFound("Component " + componentName + " has no services to promote", reader);
                        context.addError(error);
                        continue;
                    } else {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted service must be specified for " + name, reader);
                        context.addError(error);
                        continue;
                    }
                }
            }
            processServiceContract(service, promotedService, reader, context);
        }
    }

    private void updateAndValidateReferencePromotions(Composite type, XMLStreamReader reader, IntrospectionContext context) {
        for (ReferenceDefinition definition : type.getReferences().values()) {
            CompositeReference reference = (CompositeReference) definition;
            for (URI promotedUri : reference.getPromotedUris()) {
                String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
                ComponentDefinition<?> promoted = type.getComponents().get(componentName);
                String referenceName = promotedUri.getFragment();
                if (promoted == null) {
                    PromotionNotFound error =
                            new PromotionNotFound("Component " + componentName + " referenced by " + reference.getName() + " not found", reader);
                    context.addError(error);
                    return;
                } else {
                    if (referenceName == null && promoted.getComponentType().getReferences().size() != 1) {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted reference must be specified for " + reference.getName(), reader);
                        context.addError(error);
                        continue;
                    }
                    ReferenceDefinition promotedReference;
                    if (referenceName == null && promoted.getComponentType().getReferences().size() == 1) {
                        promotedReference = promoted.getComponentType().getReferences().values().iterator().next();
                    } else {
                        promotedReference = promoted.getComponentType().getReferences().get(referenceName);
                    }
                    if (referenceName != null && promotedReference == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Reference " + referenceName + " promoted by " + reference.getName() + " not found", reader);
                        context.addError(error);
                        continue;
                    }
                    processMultiplicity(reference, promotedReference, reader, context);
                    processReferenceContract(reference, promotedReference, reader, context);
                }

            }
        }
    }

    /**
     * Sets the composite service contract from the promoted service if not explicitly configured. If configured, validates the contract matches the
     * promoted reference contract.
     *
     * @param service         the service
     * @param promotedService the promoted service
     * @param reader          the reader
     * @param context         the context
     */
    private void processServiceContract(CompositeService service,
                                        ServiceDefinition promotedService,
                                        XMLStreamReader reader,
                                        IntrospectionContext context) {
        if (service.getServiceContract() == null) {
            // if a service contract is not set on the composite service, inherit from the promoted service
            service.setServiceContract(promotedService.getServiceContract());
        } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
            // verify service contracts are compatible - the composite service contract can be a subset of the promoted service contract
            MatchResult result = contractMatcher.isAssignableFrom(service.getServiceContract(), promotedService.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = service.getName();
                IncompatibleContracts error =
                        new IncompatibleContracts("The composite service interface " + name + " is not compatible with the promoted service "
                                + promotedService.getName() + ": " + result.getError(), reader);
                context.addError(error);
            } else {
                matchServiceCallbackContracts(service, promotedService, reader, context);
            }
        }
    }

    /**
     * Sets the composite reference service contract from the promoted reference if not explicitly configured. If configured, validates the contract
     * matches the promoted reference contract.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param reader            the reader
     * @param context           the context
     */
    private void processReferenceContract(CompositeReference reference,
                                          ReferenceDefinition promotedReference,
                                          XMLStreamReader reader,
                                          IntrospectionContext context) {
        if (reference.getServiceContract() == null) {
            // if a reference contract is not set on the composite service, inherit from the promoted reference
            reference.setServiceContract(promotedReference.getServiceContract());
        } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
            // verify service contracts are compatible - the promoted reference contract can be a subset of the composite reference contract
            ServiceContract promotedContract = promotedReference.getServiceContract();
            ServiceContract contract = reference.getServiceContract();
            MatchResult result = contractMatcher.isAssignableFrom(promotedContract, contract, true);
            if (!result.isAssignable()) {
                String name = reference.getName();
                IncompatibleContracts error = new IncompatibleContracts("The composite service interface " + name
                        + " is not compatible with the promoted service " + promotedReference.getName() + ": " + result.getError(), reader);
                context.addError(error);
            } else {
                matchReferenceCallbackContracts(reference, promotedReference, reader, context);
            }
        }
    }

    /**
     * Matches the service contract declared on the promoted service and the component service.
     *
     * @param service         the service
     * @param promotedService the component type service
     * @param reader          the reader
     * @param context         the context
     */
    private void matchServiceCallbackContracts(CompositeService service,
                                               ServiceDefinition promotedService,
                                               XMLStreamReader reader,
                                               IntrospectionContext context) {
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract promotedCallbackContract = promotedService.getServiceContract().getCallbackContract();
        if (promotedCallbackContract == null) {
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for service " + service.getName() + " does not have a callback contract", reader);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(promotedCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            String name = service.getName();
            IncompatibleContracts error = new IncompatibleContracts("The composite service " + name + " callback contract is not compatible with " +
                    "the promoted service " + promotedService.getName() + " callback contract: " + result.getError(), reader);
            context.addError(error);
        }
    }

    /**
     * Matches the service contract declared on the promoted reference and the component reference.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param reader            the reader
     * @param context           the context
     */
    private void matchReferenceCallbackContracts(CompositeReference reference,
                                                 ReferenceDefinition promotedReference,
                                                 XMLStreamReader reader,
                                                 IntrospectionContext context) {
        ServiceContract callbackContract = reference.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract promotedCallbackContract = promotedReference.getServiceContract().getCallbackContract();
        if (promotedCallbackContract == null) {
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for reference " + reference.getName() + " does not have a callback contract", reader);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(promotedCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            String name = reference.getName();
            IncompatibleContracts error = new IncompatibleContracts("The composite reference " + name + " callback contract is not compatible with " +
                    "the promoted reference " + promotedReference.getName() + " callback contract: " + result.getError(), reader);
            context.addError(error);
        }
    }


    /**
     * Sets the composite multiplicity to inherit from the promoted reference if not explicitly configured. If configured, validates the setting
     * against the promoted setting.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param reader            the reader
     * @param context           the context
     */
    private void processMultiplicity(CompositeReference reference,
                                     ReferenceDefinition promotedReference,
                                     XMLStreamReader reader,
                                     IntrospectionContext context) {
        // set the multiplicity to inherit from the promoted reference
        if (reference.getMultiplicity() == null) {
            Multiplicity multiplicity = promotedReference.getMultiplicity();
            reference.setMultiplicity(multiplicity);
        } else {
            String name = reference.getName();
            if (!loaderHelper.canNarrow(reference.getMultiplicity(), promotedReference.getMultiplicity())) {
                InvalidValue failure = new InvalidValue("The multiplicity setting for reference " + name + " widens the default setting", reader);
                context.addError(failure);
            }

        }
    }

    private void updateContext(IntrospectionContext context, IntrospectionContext childContext, QName compositeName) {
        if (childContext.hasErrors() || childContext.hasWarnings()) {
            URI uri = context.getContributionUri();
            if (childContext.hasErrors()) {
                ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                artifactFailure.addFailures(childContext.getErrors());
                context.addError(artifactFailure);
            }
            if (childContext.hasWarnings()) {
                ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                artifactFailure.addFailures(childContext.getWarnings());
                context.addWarning(artifactFailure);
            }
        }
    }

    private NamespaceContext createNamespaceContext(XMLStreamReader reader) {
        StatefulNamespaceContext namespaceContext = new StatefulNamespaceContext();
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getNamespacePrefix(i);
            if (prefix == null) {
                continue;
            }
            String namespaceUri = reader.getNamespaceURI(prefix);
            namespaceContext.addNamespace(prefix, namespaceUri);
        }
        return namespaceContext;
    }


}
