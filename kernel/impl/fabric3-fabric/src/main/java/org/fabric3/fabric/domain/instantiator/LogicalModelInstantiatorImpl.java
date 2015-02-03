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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.instantiator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

/**
 *
 */
public class LogicalModelInstantiatorImpl implements LogicalModelInstantiator {
    /**
     * Represents a synthetic composite. Synthetic composites are created to instantiate multiple deployable composites in a single operation.
     */
    private static final QName SYNTHETIC_COMPOSITE = new QName(org.fabric3.api.Namespaces.F3, "SyntheticComposite");

    private ChannelInstantiator channelInstantiator;
    private AtomicComponentInstantiator atomicComponentInstantiator;
    private CompositeComponentInstantiator compositeComponentInstantiator;
    private WireInstantiator wireInstantiator;
    private AutowireInstantiator autowireInstantiator;

    @Constructor
    public LogicalModelInstantiatorImpl(@Reference CompositeComponentInstantiator compositeComponentInstantiator,
                                        @Reference AtomicComponentInstantiator atomicComponentInstantiator,
                                        @Reference WireInstantiator wireInstantiator,
                                        @Reference AutowireInstantiator autowireInstantiator,
                                        @Reference ChannelInstantiator channelInstantiator) {
        this.channelInstantiator = channelInstantiator;
        this.atomicComponentInstantiator = atomicComponentInstantiator;
        this.compositeComponentInstantiator = compositeComponentInstantiator;
        this.wireInstantiator = wireInstantiator;
        this.autowireInstantiator = autowireInstantiator;
    }

    public LogicalModelInstantiatorImpl(CompositeComponentInstantiator compositeComponentInstantiator,
                                        AtomicComponentInstantiator atomicComponentInstantiator,
                                        WireInstantiator wireInstantiator,
                                        AutowireInstantiator autowireInstantiator) {
        this(compositeComponentInstantiator, atomicComponentInstantiator, wireInstantiator, autowireInstantiator, null);
    }

    public InstantiationContext include(Composite composite, LogicalCompositeComponent domain) {
        return include(domain, composite, false);
    }

    public InstantiationContext include(List<Composite> composites, LogicalCompositeComponent domain) {
        Composite composite = synthesizeComposite(composites);
        return include(domain, composite, true);
    }

    private InstantiationContext include(LogicalCompositeComponent domain, Composite composite, boolean synthetic) {
        InstantiationContext context = new InstantiationContext();

        // merge the property values into the parent
        includeProperties(composite, domain, context);

        // merge resources
        includeResources(composite, domain, synthetic);

        // instantiate all the components in the composite and add them to the parent
        instantiate(composite, domain, synthetic, context);

        // resolve services and references - evaluate all references since reinjection may apply
        if (domain.isAutowire()) {
            for (LogicalComponent<?> component : domain.getComponents()) {
                autowireInstantiator.instantiate(component, context);
            }
        }
        return context;
    }

    private void includeProperties(Composite composite, LogicalCompositeComponent domain, InstantiationContext context) {
        for (Property property : composite.getProperties().values()) {
            String name = property.getName();
            if (domain.getAllProperties().containsKey(name)) {
                DuplicateProperty error = new DuplicateProperty(name, domain);
                context.addError(error);
            } else {
                Document value = property.getDefaultValue();
                boolean many = property.isMany();
                QName type = property.getType();
                LogicalProperty logicalProperty = new LogicalProperty(name, value, many, type, domain);
                domain.setProperties(logicalProperty);
            }
        }
    }

    private void includeResources(Composite composite, LogicalCompositeComponent domain, boolean synthetic) {
        if (synthetic) {
            for (Include include : composite.getIncludes().values()) {
                Composite included = include.getIncluded();
                for (Resource definition : included.getResources()) {
                    LogicalResource<?> resource = new LogicalResource<>(definition, domain);
                    resource.setDeployable(included.getName());
                    domain.addResource(resource);
                }
            }
        } else {
            for (Resource definition : composite.getResources()) {
                LogicalResource<?> resource = new LogicalResource<>(definition, domain);
                resource.setDeployable(composite.getName());
                domain.addResource(resource);
            }
        }
    }

    /**
     * Instantiates a composite and its children in a domain
     *
     * @param composite the composite definition to instantiate
     * @param domain    the domain logical components
     * @param synthetic true if the composite is synthetic and its children should be treated as a single deployment unit
     * @param context   the instantiation context
     */
    private void instantiate(Composite composite, LogicalCompositeComponent domain, boolean synthetic, InstantiationContext context) {
        // instantiate the declared components
        Collection<Component<? extends Implementation<?>>> definitions = composite.getDeclaredComponents().values();
        List<LogicalComponent<?>> newComponents = new ArrayList<>(definitions.size());
        for (Component<? extends Implementation<?>> definition : definitions) {
            LogicalComponent<?> logicalComponent = instantiate(definition, domain, context);
            setDeployable(logicalComponent, composite.getName());
            newComponents.add(logicalComponent);
        }

        // instantiate the included components
        instantiateIncludes(composite, newComponents, synthetic, domain, context);

        // instantiate wires - note this must be done after the included components as wire targets may resolve to an included service
        wireInstantiator.instantiateCompositeWires(composite, domain, context);
        for (LogicalComponent<?> component : newComponents) {
            wireInstantiator.instantiateReferenceWires(component, context);
        }

        if (channelInstantiator != null) {  // during bootstrap channel support is not available
            // instantiate channels
            if (synthetic) {
                for (Include include : composite.getIncludes().values()) {
                    // If it is a synthetic composite, included composites are the deployables.
                    // Synthetic composites are used to deploy multiple composites as a group. They include the composites (deployables).
                    // Adding the deployable name to domain-level components allows them to be managed as a group after they are deployed.
                    Composite included = include.getIncluded();
                    channelInstantiator.instantiateChannels(included, domain, context);
                }
            } else {
                channelInstantiator.instantiateChannels(composite, domain, context);

            }
        }
    }

    /**
     * Instantiates a component in the context of a parent logical composite component.
     *
     * @param definition the component definition to instantiate
     * @param parent     the parent logical composite
     * @param context    the instantiation context
     * @return the instantiated component
     */
    @SuppressWarnings("unchecked")
    private LogicalComponent<?> instantiate(Component<?> definition, LogicalCompositeComponent parent, InstantiationContext context) {
        if (definition.getComponentType() instanceof Composite) {
            Component<CompositeImplementation> component = (Component<CompositeImplementation>) definition;
            return compositeComponentInstantiator.instantiate(component, parent, context);
        } else {
            Component<Implementation<?>> component = (Component<Implementation<?>>) definition;
            return atomicComponentInstantiator.instantiate(component, parent, context);
        }
    }

    /**
     * Instantiates components in included composites.
     *
     * @param composite     the root composite to instantiate
     * @param newComponents the collection to hold instantiated components
     * @param synthetic     true if the root composite is synthetic
     * @param domain        the domain
     * @param context       the instantiation context
     */
    private void instantiateIncludes(Composite composite,
                                     List<LogicalComponent<?>> newComponents,
                                     boolean synthetic,
                                     LogicalCompositeComponent domain,
                                     InstantiationContext context) {
        // instantiate the included components
        for (Include include : composite.getIncludes().values()) {
            for (Component<? extends Implementation<?>> definition : include.getIncluded().getComponents().values()) {
                LogicalComponent<?> logicalComponent = instantiate(definition, domain, context);
                if (synthetic) {
                    // If it is a synthetic composite, included composites are the deployables.
                    // Synthetic composites are used to deploy multiple composites as a group. They include the composites (deployables).
                    // Adding the deployable name to domain-level components allows them to be managed as a group after they are deployed.
                    setDeployable(logicalComponent, include.getIncluded().getName());
                } else {
                    setDeployable(logicalComponent, composite.getName());
                }
                newComponents.add(logicalComponent);
                // add to the domain since includes starting from a deployable composite are "collapsed" to the domain level
                domain.addComponent(logicalComponent);
            }
        }
    }

    /**
     * Synthesizes a composite from a collection of composites using inclusion.  A counter is maintained for each include name and used to generate include
     * names in situations where they may clash, e.g. when two composites from different contributions with the same name are deployed together.
     *
     * @param composites the composites to synthesize
     * @return the synthesized composite
     */
    private Composite synthesizeComposite(List<Composite> composites) {
        Composite synthesized = new Composite(SYNTHETIC_COMPOSITE);
        Map<QName, AtomicInteger> counters = new HashMap<>();
        for (Composite composite : composites) {
            Include include = new Include();
            QName name = composite.getName();
            include.setName(name);
            include.setIncluded(composite);
            if (synthesized.getIncludes().containsKey(name)) {
                AtomicInteger counter = counters.get(name);
                if (counter == null) {
                    counter = new AtomicInteger();
                    counters.put(name, counter);
                }
                include.setName(new QName(name.getNamespaceURI(), name.getLocalPart() + counter.incrementAndGet()));
            }
            synthesized.add(include);

        }
        return synthesized;
    }

    /**
     * Recursively sets the deployable composite the logical component was instantiated from.
     *
     * @param component  the logical component
     * @param deployable the deployable
     */
    private void setDeployable(LogicalComponent<?> component, QName deployable) {
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                setDeployable(child, deployable);
            }
            for (LogicalResource<?> resource : composite.getResources()) {
                resource.setDeployable(deployable);
            }
            for (LogicalChannel channel : composite.getChannels()) {
                channel.setDeployable(deployable);
            }
        }
        component.setDeployable(deployable);
    }

}
