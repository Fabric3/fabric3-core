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
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
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
        NamespaceContext namespace = reader.getNamespaceContext();
        String constrainingTypeAttrbute = reader.getAttributeValue(null, "constrainingType");
        QName constrainingType = LoaderUtil.getQName(constrainingTypeAttrbute, targetNamespace, namespace);

        Composite type = new Composite(compositeName);
        type.setLocal(local);
        type.setAutowire(Autowire.fromString(reader.getAttributeValue(null, "autowire")));
        type.setConstrainingType(constrainingType);
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
                    } else if (REFERENCE.equals(qname)) {
                        handleReference(type, reader, childContext);
                        continue;
                    } else if (COMPONENT.equals(qname)) {
                        boolean valid = handleComponent(type, reader, childContext);
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

    private void handleExtensionElement(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        // Extension element - for now try to load and see if we can handle it
        ModelObject modelObject;
        try {
            modelObject = registry.load(reader, ModelObject.class, childContext);
            // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
            // UnrecognizedElement added to the context if none is found
        } catch (UnrecognizedElementException e) {
            UnrecognizedElement failure = new UnrecognizedElement(reader);
            childContext.addError(failure);
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
            childContext.addError(new UnrecognizedElement(reader));
        }
    }

    private void handleWire(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException, UnrecognizedElementException {
        WireDefinition wire = registry.load(reader, WireDefinition.class, childContext);
        if (wire == null) {
            // errror encountered loading the wire
            return;
        }
        type.add(wire);
    }

    private boolean handleComponent(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException, UnrecognizedElementException {
        ComponentDefinition<?> componentDefinition = registry.load(reader, ComponentDefinition.class, childContext);
        if (componentDefinition == null) {
            // errror encountered loading the componentDefinition
            return false;
        }
        String key = componentDefinition.getName();
        if (type.getComponents().containsKey(key)) {
            DuplicateComponentName failure = new DuplicateComponentName(key, reader);
            childContext.addError(failure);
            return false;
        }
        Implementation<?> implementation = componentDefinition.getImplementation();
        if (implementation == null || implementation.getComponentType() == null) {
            return false;
        }
        if (type.getAutowire() != Autowire.INHERITED && componentDefinition.getAutowire() == Autowire.INHERITED) {
            componentDefinition.setAutowire(type.getAutowire());
        }
        type.add(componentDefinition);
        return true;
    }

    private void handleReference(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException {
        CompositeReference reference = referenceLoader.load(reader, childContext);
        if (reference == null) {
            // errror encountered loading the reference
            return;
        }
        if (type.getReferences().containsKey(reference.getName())) {
            String key = reference.getName();
            DuplicatePromotedReferenceName failure = new DuplicatePromotedReferenceName(key, reader);
            childContext.addError(failure);
        } else {
            type.add(reference);
        }
    }

    private void handleService(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException {
        CompositeService service = serviceLoader.load(reader, childContext);
        if (service == null) {
            // errror encountered loading the service
            return;
        }
        if (type.getServices().containsKey(service.getName())) {
            String key = service.getName();
            DuplicateService failure = new DuplicateService(key, reader);
            childContext.addError(failure);
        } else {
            type.add(service);
        }
    }

    private void handleProperty(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException, UnrecognizedElementException {
        Property property = propertyLoader.load(reader, childContext);
        if (property == null) {
            // errror encountered loading the property
            return;
        }
        String key = property.getName();
        if (type.getProperties().containsKey(key)) {
            DuplicateProperty failure = new DuplicateProperty(key, reader);
            childContext.addError(failure);
        } else {
            type.add(property);
        }
    }

    private void handleInclude(Composite type, XMLStreamReader reader, IntrospectionContext childContext)
            throws XMLStreamException, UnrecognizedElementException {
        Include include = registry.load(reader, Include.class, childContext);
        if (include == null) {
            // errror encountered loading the include
            return;
        }
        QName includeName = include.getName();
        if (type.getIncludes().containsKey(includeName)) {
            String identifier = includeName.toString();
            DuplicateInclude failure = new DuplicateInclude(identifier, reader);
            childContext.addError(failure);
            return;
        }
        for (ComponentDefinition definition : include.getIncluded().getComponents().values()) {
            String key = definition.getName();
            if (type.getComponents().containsKey(key)) {
                DuplicateComponentName failure = new DuplicateComponentName(key, reader);
                childContext.addError(failure);
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

    private void updateAndValidateServicePromotions(Composite type, XMLStreamReader reader, IntrospectionContext childContext) {
        for (CompositeService service : type.getServices().values()) {
            URI promotedUri = service.getPromote();
            String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
            ComponentDefinition promotedComponent = type.getComponents().get(componentName);
            ServiceDefinition promotedService;
            String name = service.getName();
            if (promotedComponent == null) {
                PromotionNotFound error =
                        new PromotionNotFound("Component " + componentName + " referenced by " + name + " not found", reader);
                childContext.addError(error);
                continue;
            } else {
                String serviceName = promotedUri.getFragment();
                AbstractComponentType<?, ?, ?, ?> componentType = promotedComponent.getComponentType();
                if (serviceName != null) {
                    promotedService = componentType.getServices().get(serviceName);
                    if (promotedService == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Service " + serviceName + " promoted by " + name + " not found", reader);
                        childContext.addError(error);
                        continue;
                    }
                } else {
                    Map<String, ? extends ServiceDefinition> services = componentType.getServices();
                    int numberOfServices = services.size();
                    if (numberOfServices == 2) {
                        Iterator<? extends ServiceDefinition> iter = services.values().iterator();
                        ServiceDefinition one = iter.next();
                        ServiceDefinition two = iter.next();
                        if (!one.isManagement() && !two.isManagement()) {
                            PromotionNotFound error =
                                    new PromotionNotFound("A promoted service must be specified for " + name, reader);
                            childContext.addError(error);
                            return;
                        }
                        promotedService = one.isManagement() ? two : one;

                    } else if (numberOfServices == 1) {
                        promotedService = services.values().iterator().next();
                    } else if (numberOfServices == 0) {
                        PromotionNotFound error =
                                new PromotionNotFound("Component " + componentName + " has no services to promote", reader);
                        childContext.addError(error);
                        continue;
                    } else {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted service must be specified for " + name, reader);
                        childContext.addError(error);
                        continue;
                    }
                }
            }
            if (service.getServiceContract() == null) {
                // if a service contract is not set on the composite service, inherit from the promoted service 
                service.setServiceContract(promotedService.getServiceContract());
            } else if (contractMatcher != null) { // null check for contract matcher as it is not used during bootstrap
                // verify service contracts are compatible
                MatchResult result = contractMatcher.isAssignableFrom(promotedService.getServiceContract(), service.getServiceContract(), true);
                if (!result.isAssignable()) {
                    IncompatibleContracts error =
                            new IncompatibleContracts("The composite service interface " + name + " is not compatible with the promoted service "
                                    + promotedService.getName() + ": " + result.getError(), reader);
                    childContext.addError(error);
                }
            }
        }
    }

    private void updateAndValidateReferencePromotions(Composite type, XMLStreamReader reader, IntrospectionContext childContext) {
        for (CompositeReference reference : type.getReferences().values()) {
            for (URI promotedUri : reference.getPromotedUris()) {
                String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
                ComponentDefinition<?> promoted = type.getComponents().get(componentName);
                String referenceName = promotedUri.getFragment();
                if (promoted == null) {
                    PromotionNotFound error =
                            new PromotionNotFound("Component " + componentName + " referenced by " + reference.getName() + " not found", reader);
                    childContext.addError(error);
                    return;
                } else {
                    if (referenceName == null && promoted.getComponentType().getReferences().size() != 1) {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted reference must be specified for " + reference.getName(), reader);
                        childContext.addError(error);
                        return;
                    }
                    ReferenceDefinition promotedReference;
                    if (referenceName == null && promoted.getComponentType().getReferences().size() == 1) {
                        promotedReference = promoted.getComponentType().getReferences().get(0);
                    } else {
                        promotedReference = promoted.getComponentType().getReferences().get(referenceName);
                    }
                    if (referenceName != null && promotedReference == null) {
                        PromotionNotFound error =
                                new PromotionNotFound("Reference " + referenceName + " promoted by " + reference.getName() + " not found", reader);
                        childContext.addError(error);
                        return;
                    }
                    // set the multiplicity to inherit from the promoted reference
                    if (reference.getMultiplicity() == null) {
                        Multiplicity multiplicity = promotedReference.getMultiplicity();
                        reference.setMultiplicity(multiplicity);
                    } else {
                        if (!loaderHelper.canNarrow(reference.getMultiplicity(), promotedReference.getMultiplicity())) {
                            InvalidValue failure = new InvalidValue("The multiplicity setting for reference " + referenceName
                                    + " widens the default setting", reader);
                            childContext.addError(failure);
                        }

                    }
                }

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

}
