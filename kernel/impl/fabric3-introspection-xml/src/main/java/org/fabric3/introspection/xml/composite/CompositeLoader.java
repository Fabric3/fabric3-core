/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.Autowire;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentReference;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.api.model.type.component.WireDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.IncompatibleContracts;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.spi.introspection.xml.CompositeConstants.CHANNEL;
import static org.fabric3.spi.introspection.xml.CompositeConstants.COMPONENT;
import static org.fabric3.spi.introspection.xml.CompositeConstants.COMPOSITE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.INCLUDE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.PROPERTY;
import static org.fabric3.spi.introspection.xml.CompositeConstants.REFERENCE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.SERVICE;
import static org.fabric3.spi.introspection.xml.CompositeConstants.WIRE;

/**
 * Loads a composite component definition from an XML-based assembly file
 */
@EagerInit
public class CompositeLoader extends AbstractExtensibleTypeLoader<Composite> {
    private TypeLoader<CompositeService> serviceLoader;
    private TypeLoader<CompositeReference> referenceLoader;
    private TypeLoader<Property> propertyLoader;
    private ContractMatcher contractMatcher;
    private final LoaderHelper loaderHelper;
    boolean roundTrip;

    /**
     * Constructor used during bootstrap; service and reference elements are not supported.
     *
     * @param registry       the loader registry to register with; also used to load extension elements
     * @param propertyLoader the property loader
     * @param loaderHelper   helper the helper
     */
    public CompositeLoader(LoaderRegistry registry, TypeLoader<Property> propertyLoader, LoaderHelper loaderHelper) {
        this(registry, null, null, propertyLoader, null, loaderHelper);
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
        addAttributes("name", "autowire", "targetNamespace", "local", "requires", "policySets", "constrainingType", "channel", "schemaLocation");
        this.serviceLoader = serviceLoader;
        this.referenceLoader = referenceLoader;
        this.propertyLoader = propertyLoader;
        this.contractMatcher = contractMatcher;
        this.loaderHelper = loaderHelper;
    }

    @org.oasisopen.sca.annotation.Property(required = false)
    @Source("$systemConfig/f3:loader/@round.trip")
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    @SuppressWarnings({"VariableNotUsedInsideIf"})
    public Composite load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        // track locations so they can be used to report validation errors after the parser has been advanced
        Map<ModelObject, Location> locations = new HashMap<ModelObject, Location>();

        String name = reader.getAttributeValue(null, "name");
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        String localStr = reader.getAttributeValue(null, "local");
        boolean local = Boolean.valueOf(localStr);
        IntrospectionContext childContext = new DefaultIntrospectionContext(context, targetNamespace);
        QName compositeName = new QName(targetNamespace, name);
        NamespaceContext nsContext = createNamespaceContext(reader);

        Composite type = new Composite(compositeName);
        String autowire = reader.getAttributeValue(null, "autowire");
        type.setAutowire(Autowire.fromString(autowire));

        if (roundTrip) {
            type.enableRoundTrip();
            addNamespaces(type, reader);
            if (targetNamespace != null) {
                type.attributeSpecified("targetNamespace");
            }
            if (localStr != null) {
                type.attributeSpecified("local");
            }
            if (autowire != null) {
                type.attributeSpecified("autowire");
            }
        }
        type.setContributionUri(context.getContributionUri());
        type.setLocal(local);

        loaderHelper.loadPolicySetsAndIntents(type, reader, childContext);

        validateAttributes(reader, context, type);

        while (true) {
            int val = reader.next();
            switch (val) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (INCLUDE.equals(qname)) {
                    handleInclude(type, reader, locations, compositeName, childContext, context);
                    continue;
                } else if (PROPERTY.equals(qname)) {
                    handleProperty(type, reader, locations, childContext);
                    continue;
                } else if (SERVICE.equals(qname)) {
                    handleService(type, reader, locations, childContext);
                    continue;
                } else if (CHANNEL.equals(qname)) {
                    handleChannel(type, reader, locations, compositeName, childContext, context);
                    continue;
                } else if (REFERENCE.equals(qname)) {
                    handleReference(type, reader, locations, childContext);
                    continue;
                } else if (COMPONENT.equals(qname)) {
                    boolean valid = handleComponent(type, reader, nsContext, locations, compositeName, childContext, context);
                    if (!valid) {
                        updateContext(context, childContext, compositeName);
                    }
                    continue;
                } else if (WIRE.equals(qname)) {
                    handleWire(type, reader, compositeName, childContext, context);
                    continue;
                } else {
                    handleExtensionElement(type, reader, childContext);
                    continue;
                }
            case END_ELEMENT:
                if (!COMPOSITE.equals(reader.getName())) {
                    continue;
                }
                updateAndValidateServicePromotions(type, locations, childContext);
                updateAndValidateReferencePromotions(type, locations, childContext);
                updateContext(context, childContext, compositeName);
                return type;
            case XMLStreamReader.COMMENT:
                if (!roundTrip) {
                    continue;
                }
                String comment = reader.getText();
                type.addComment(comment);
                continue;
            default:
                if (!roundTrip) {
                    continue;
                }
                comment = reader.getText();
                type.addText(comment);
            }

        }
    }

    private void addNamespaces(Composite type, XMLStreamReader reader) {
        int count = reader.getNamespaceCount();
        for (int i = 0; i < count; i++) {
            String prefix = reader.getNamespacePrefix(i);
            String uri = reader.getNamespaceURI(i);
            type.addNamespace(prefix, uri);
        }
    }

    private void handleExtensionElement(Composite type, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        // Extension element - for now try to load and see if we can handle it
        ModelObject modelObject = registry.load(reader, ModelObject.class, context);
        // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
        // UnrecognizedElement added to the context if none is found
        if (modelObject instanceof Property) {
            type.add((Property) modelObject);
        } else if (modelObject instanceof CompositeService) {
            type.add((CompositeService) modelObject);
        } else if (modelObject instanceof CompositeReference) {
            type.add((CompositeReference) modelObject);
        } else if (modelObject instanceof ComponentDefinition) {
            type.add((ComponentDefinition<?>) modelObject);
        } else if (modelObject instanceof ResourceDefinition) {
            type.add((ResourceDefinition) modelObject);
        } else if (modelObject == null) {
            // loaders may elect to return a null element; ignore
        } else {
            UnrecognizedElement failure = new UnrecognizedElement(reader, startLocation, type);
            context.addError(failure);
        }
    }

    private void handleWire(Composite type,
                            XMLStreamReader reader,
                            QName compositeName,
                            IntrospectionContext context,
                            IntrospectionContext parentContext) throws XMLStreamException {
        WireDefinition wire;
        wire = registry.load(reader, WireDefinition.class, context);
        if (wire == null) {
            // error encountered loading the wire
            updateContext(parentContext, context, compositeName);
            return;
        }
        type.add(wire);
    }

    private boolean handleComponent(Composite type,
                                    XMLStreamReader reader,
                                    NamespaceContext nsContext,
                                    Map<ModelObject, Location> locations,
                                    QName compositeName,
                                    IntrospectionContext context,
                                    IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        ComponentDefinition<?> componentDefinition = registry.load(reader, ComponentDefinition.class, context);
        if (componentDefinition == null) {
            // error encountered loading the componentDefinition
            updateContext(parentContext, context, compositeName);
            return false;
        }
        String key = componentDefinition.getName();
        if (type.getComponents().containsKey(key)) {
            DuplicateComponentName failure = new DuplicateComponentName(key, startLocation, type);
            context.addError(failure);
            return false;
        }
        type.add(componentDefinition);
        Implementation<?> implementation = componentDefinition.getImplementation();
        if (implementation == null || implementation.getComponentType() == null) {
            return false;
        }
        if (type.getAutowire() != Autowire.INHERITED && componentDefinition.getAutowire() == Autowire.INHERITED) {
            componentDefinition.setAutowire(type.getAutowire());
        }

        // Calculate the namespace context from the composite element since XMLStreamReader.getNamespaceCount() only returns the number of namespaces
        // declared on the current element. This means namespaces defined on parent elements which are active (e.g. <composite>) or not reported.
        // Scoping results in no namespaces being reported 
        for (PropertyValue value : componentDefinition.getPropertyValues().values()) {
            value.setNamespaceContext(nsContext);
        }
        locations.put(componentDefinition, startLocation);
        return true;
    }

    private void handleChannel(Composite type,
                               XMLStreamReader reader,
                               Map<ModelObject, Location> locations,
                               QName compositeName,
                               IntrospectionContext context,
                               IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        ChannelDefinition channelDefinition = registry.load(reader, ChannelDefinition.class, context);
        if (channelDefinition == null) {
            // error encountered loading the channel definition
            updateContext(parentContext, context, compositeName);
            return;
        }
        String key = channelDefinition.getName();
        if (type.getChannels().containsKey(key)) {
            DuplicateChannelName failure = new DuplicateChannelName(key, startLocation, type);
            context.addError(failure);
            return;
        }
        locations.put(channelDefinition, startLocation);
        type.add(channelDefinition);
    }

    private void handleReference(Composite type, XMLStreamReader reader, Map<ModelObject, Location> locations, IntrospectionContext context)
            throws XMLStreamException {
        Location startLocation = reader.getLocation();
        CompositeReference reference = referenceLoader.load(reader, context);
        if (reference == null) {
            // error encountered loading the reference
            return;
        }
        if (type.getReferences().containsKey(reference.getName())) {
            String key = reference.getName();
            DuplicatePromotedReference failure = new DuplicatePromotedReference(key, startLocation, type);
            context.addError(failure);
        } else {
            type.add(reference);
            locations.put(reference, startLocation);
        }
    }

    private void handleService(Composite type, XMLStreamReader reader, Map<ModelObject, Location> locations, IntrospectionContext context)
            throws XMLStreamException {
        Location startLocation = reader.getLocation();
        CompositeService service = serviceLoader.load(reader, context);
        if (service == null) {
            // error encountered loading the service
            return;
        }
        if (type.getServices().containsKey(service.getName())) {
            String key = service.getName();
            DuplicatePromotedService failure = new DuplicatePromotedService(key, startLocation, type);
            context.addError(failure);
        } else {
            locations.put(service, startLocation);
            type.add(service);
        }
    }

    private void handleProperty(Composite type, XMLStreamReader reader, Map<ModelObject, Location> locations, IntrospectionContext context)
            throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Property property = propertyLoader.load(reader, context);
        if (property == null) {
            // error encountered loading the property
            return;
        }
        String key = property.getName();
        if (type.getProperties().containsKey(key)) {
            DuplicateProperty failure = new DuplicateProperty(key, startLocation, type);
            context.addError(failure);
        } else {
            type.add(property);
            locations.put(property, startLocation);
        }
    }

    private void handleInclude(Composite type,
                               XMLStreamReader reader,
                               Map<ModelObject, Location> locations,
                               QName compositeName,
                               IntrospectionContext context,
                               IntrospectionContext parentContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        Include include = registry.load(reader, Include.class, context);
        if (include == null) {
            // error encountered loading the include
            updateContext(parentContext, context, compositeName);
            return;
        }
        QName includeName = include.getName();
        if (type.getIncludes().containsKey(includeName)) {
            String identifier = includeName.toString();
            DuplicateInclude failure = new DuplicateInclude(identifier, startLocation, include);
            context.addError(failure);
            return;
        }
        Composite included = include.getIncluded();
        if (included == null) {
            return;
        }
        if (type.isLocal() != included.isLocal()) {
            InvalidInclude error = new InvalidInclude("Composite " + type.getName() + " has a local value of " + type.isLocal()
                                                              + " and the included composite " + includeName + " has a value of "
                                                              + included.isLocal(), startLocation);
            context.addError(error);
        }
        for (ComponentDefinition definition : included.getComponents().values()) {
            String key = definition.getName();
            if (type.getComponents().containsKey(key)) {
                DuplicateComponentName failure = new DuplicateComponentName(key, startLocation, type);
                context.addError(failure);
            }
        }
        locations.put(include, startLocation);
        type.add(include);
    }

    private void updateAndValidateServicePromotions(Composite type, Map<ModelObject, Location> locations, IntrospectionContext context) {
        for (AbstractService definition : type.getServices().values()) {
            CompositeService service = (CompositeService) definition;
            Location location = locations.get(service);
            URI promotedUri = service.getPromote();
            if (promotedUri == null) {
                String serviceName = service.getName();
                QName compositeName = type.getName();
                MissingAttribute error =
                        new MissingAttribute("Service promotion not specified for " + serviceName + " in composite " + compositeName, location);
                context.addError(error);
                continue;
            }
            String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
            ComponentDefinition promotedComponent = type.getComponents().get(componentName);
            AbstractService promotedService;
            String name = service.getName();
            if (promotedComponent == null) {
                PromotionNotFound error =
                        new PromotionNotFound("Component " + componentName + " referenced by " + name + " not found", location, service);
                context.addError(error);
                continue;
            } else {
                String serviceName = promotedUri.getFragment();
                ComponentType componentType = promotedComponent.getComponentType();
                if (serviceName != null) {
                    promotedService = componentType.getServices().get(serviceName);
                    if (promotedService == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Service " + serviceName + " promoted by " + name + " not found", location, service);
                        context.addError(error);
                        continue;
                    }
                } else {
                    Map<String, AbstractService> services = componentType.getServices();
                    int numberOfServices = services.size();
                    if (numberOfServices == 2) {
                        PromotionNotFound error = new PromotionNotFound("A promoted service must be specified for " + name, location, service);
                        context.addError(error);
                        return;

                    } else if (numberOfServices == 1) {
                        promotedService = services.values().iterator().next();
                    } else if (numberOfServices == 0) {
                        PromotionNotFound error =
                                new PromotionNotFound("Component " + componentName + " has no services to promote", location, service);
                        context.addError(error);
                        continue;
                    } else {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted service must be specified for " + name, location, service);
                        context.addError(error);
                        continue;
                    }
                }
            }
            processServiceContract(service, promotedService, locations, context);
        }
    }

    private void updateAndValidateReferencePromotions(Composite type, Map<ModelObject, Location> locations, IntrospectionContext context) {
        for (AbstractReference definition : type.getReferences().values()) {
            CompositeReference reference = (CompositeReference) definition;
            Location location = locations.get(reference);
            for (URI promotedUri : reference.getPromotedUris()) {
                String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
                ComponentDefinition<?> promoted = type.getComponents().get(componentName);
                String referenceName = promotedUri.getFragment();
                if (promoted == null) {
                    PromotionNotFound error =
                            new PromotionNotFound("Component " + componentName + " referenced by " + reference.getName() + " not found",
                                                  location,
                                                  reference);
                    context.addError(error);
                    return;
                } else {
                    if (referenceName == null && promoted.getComponentType().getReferences().size() != 1) {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted reference must be specified for " + reference.getName(), location, reference);
                        context.addError(error);
                        continue;
                    }
                    AbstractReference promotedReference;
                    if (referenceName == null && promoted.getComponentType().getReferences().size() == 1) {
                        promotedReference = promoted.getComponentType().getReferences().values().iterator().next();
                    } else {
                        promotedReference = promoted.getComponentType().getReferences().get(referenceName);
                    }
                    if (referenceName != null && promotedReference == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Reference " + referenceName + " promoted by " + reference.getName() + " not found",
                                                      location,
                                                      reference);
                        context.addError(error);
                        continue;
                    }
                    processMultiplicity(reference, promotedReference, location, context);
                    processReferenceContract(reference, promotedReference, locations, context);
                    // check overridable
                    ComponentReference componentReference = promoted.getReferences().get(referenceName);
                    if (componentReference != null) {
                        if (componentReference.isNonOverridable()) {
                            if (promotedReference.getMultiplicity().equals(Multiplicity.ONE_ONE) || promotedReference.getMultiplicity().equals(
                                    Multiplicity.ZERO_ONE)) {
                                IllegalPromotion failure =
                                        new IllegalPromotion("Cannot promote a 0..1 or 1..1 non-overridable reference: " + referenceName,
                                                             location,
                                                             promotedReference);
                                context.addError(failure);
                            }
                        }
                    }
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
     * @param locations       the location mappings
     * @param context         the context
     */
    private void processServiceContract(CompositeService service,
                                        AbstractService promotedService,
                                        Map<ModelObject, Location> locations,
                                        IntrospectionContext context) {
        if (service.getServiceContract() == null) {
            // if a service contract is not set on the composite service, inherit from the promoted service
            service.setServiceContract(promotedService.getServiceContract());
        } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
            // verify service contracts are compatible - the composite service contract can be a subset of the promoted service contract
            MatchResult result = contractMatcher.isAssignableFrom(service.getServiceContract(), promotedService.getServiceContract(), true);
            if (!result.isAssignable()) {
                String name = service.getName();
                Location location = locations.get(service);
                IncompatibleContracts error =
                        new IncompatibleContracts("The composite service interface " + name + " is not compatible with the promoted service "
                                                          + promotedService.getName() + ": " + result.getError(), location, service);
                context.addError(error);
            } else {
                matchServiceCallbackContracts(service, promotedService, locations, context);
            }
        }
    }

    /**
     * Sets the composite reference service contract from the promoted reference if not explicitly configured. If configured, validates the contract
     * matches the promoted reference contract.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param locations         the locations
     * @param context           the context
     */
    private void processReferenceContract(CompositeReference reference,
                                          AbstractReference promotedReference,
                                          Map<ModelObject, Location> locations,
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
                Location location = locations.get(reference);
                IncompatibleContracts error = new IncompatibleContracts("The composite service interface " + name
                                                                                + " is not compatible with the promoted service "
                                                                                + promotedReference.getName() + ": " + result.getError(),
                                                                        location, reference);
                context.addError(error);
            } else {
                matchReferenceCallbackContracts(reference, promotedReference, locations, context);
            }
        }
    }

    /**
     * Matches the service contract declared on the promoted service and the component service.
     *
     * @param service         the service
     * @param promotedService the component type service
     * @param locations       the locations
     * @param context         the context
     */
    private void matchServiceCallbackContracts(CompositeService service,
                                               AbstractService promotedService,
                                               Map<ModelObject, Location> locations,
                                               IntrospectionContext context) {
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract promotedCallbackContract = promotedService.getServiceContract().getCallbackContract();
        if (promotedCallbackContract == null) {
            Location location = locations.get(service);
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for service " + service.getName() + " does not have a callback contract",
                                              location,
                                              service);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(promotedCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            Location location = locations.get(service);
            String name = service.getName();
            IncompatibleContracts error = new IncompatibleContracts("The composite service " + name + " callback contract is not compatible with " +
                                                                            "the promoted service " + promotedService.getName()
                                                                            + " callback contract: " + result.getError(), location, service);
            context.addError(error);
        }
    }

    /**
     * Matches the service contract declared on the promoted reference and the component reference.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param locations         the locations
     * @param context           the context
     */
    private void matchReferenceCallbackContracts(CompositeReference reference,
                                                 AbstractReference promotedReference,
                                                 Map<ModelObject, Location> locations,
                                                 IntrospectionContext context) {
        ServiceContract callbackContract = reference.getServiceContract().getCallbackContract();
        if (callbackContract == null) {
            return;
        }
        ServiceContract promotedCallbackContract = promotedReference.getServiceContract().getCallbackContract();
        if (promotedCallbackContract == null) {
            Location location = locations.get(reference);
            IncompatibleContracts error =
                    new IncompatibleContracts("Component type for reference " + reference.getName() + " does not have a callback contract",
                                              location,
                                              reference);
            context.addError(error);
            return;
        }
        MatchResult result = contractMatcher.isAssignableFrom(promotedCallbackContract, callbackContract, true);
        if (!result.isAssignable()) {
            Location location = locations.get(reference);
            String name = reference.getName();
            IncompatibleContracts error = new IncompatibleContracts("The composite reference " + name + " callback contract is not compatible with " +
                                                                            "the promoted reference " + promotedReference.getName()
                                                                            + " callback contract: " + result.getError(), location, reference);
            context.addError(error);
        }
    }


    /**
     * Sets the composite multiplicity to inherit from the promoted reference if not explicitly configured. If configured, validates the setting
     * against the promoted setting.
     *
     * @param reference         the reference
     * @param promotedReference the promoted reference
     * @param location          the current location
     * @param context           the context
     */
    private void processMultiplicity(CompositeReference reference,
                                     AbstractReference promotedReference,
                                     Location location,
                                     IntrospectionContext context) {
        // set the multiplicity to inherit from the promoted reference
        if (reference.getMultiplicity() == null) {
            Multiplicity multiplicity = promotedReference.getMultiplicity();
            reference.setMultiplicity(multiplicity);
        } else {
            String name = reference.getName();
            if (!loaderHelper.canNarrow(reference.getMultiplicity(), promotedReference.getMultiplicity())) {
                InvalidValue failure = new InvalidValue("The multiplicity setting for reference " + name + " widens the default setting", location);
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
