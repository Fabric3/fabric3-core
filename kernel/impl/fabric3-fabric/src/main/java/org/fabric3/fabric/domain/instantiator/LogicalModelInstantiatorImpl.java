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
*/
package org.fabric3.fabric.domain.instantiator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalResource;
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
    private PromotionNormalizer promotionNormalizer;
    private AtomicComponentInstantiator atomicComponentInstantiator;
    private CompositeComponentInstantiator compositeComponentInstantiator;
    private WireInstantiator wireInstantiator;
    private AutowireNormalizer autowireNormalizer;
    private PromotionResolutionService promotionResolutionService;
    private AutowireInstantiator autowireInstantiator;

    @Constructor
    public LogicalModelInstantiatorImpl(@Reference CompositeComponentInstantiator compositeComponentInstantiator,
                                        @Reference AtomicComponentInstantiator atomicComponentInstantiator,
                                        @Reference WireInstantiator wireInstantiator,
                                        @Reference AutowireInstantiator autowireInstantiator,
                                        @Reference ChannelInstantiator channelInstantiator,
                                        @Reference PromotionNormalizer promotionNormalizer,
                                        @Reference AutowireNormalizer autowireNormalizer,
                                        @Reference PromotionResolutionService promotionResolutionService) {
        this.channelInstantiator = channelInstantiator;
        this.promotionNormalizer = promotionNormalizer;
        this.atomicComponentInstantiator = atomicComponentInstantiator;
        this.compositeComponentInstantiator = compositeComponentInstantiator;
        this.wireInstantiator = wireInstantiator;
        this.autowireNormalizer = autowireNormalizer;
        this.promotionResolutionService = promotionResolutionService;
        this.autowireInstantiator = autowireInstantiator;
    }

    public LogicalModelInstantiatorImpl(CompositeComponentInstantiator compositeComponentInstantiator,
                                        AtomicComponentInstantiator atomicComponentInstantiator,
                                        WireInstantiator wireInstantiator,
                                        AutowireInstantiator autowireInstantiator,
                                        PromotionNormalizer promotionNormalizer,
                                        AutowireNormalizer autowireNormalizer,
                                        PromotionResolutionService promotionResolutionService) {
        this(compositeComponentInstantiator,
             atomicComponentInstantiator,
             wireInstantiator,
             autowireInstantiator,
             null,
             promotionNormalizer,
             autowireNormalizer,
             promotionResolutionService);
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
        List<LogicalComponent<?>> newComponents = instantiate(composite, domain, synthetic, context);

        // normalize autowire settings and bindings for each new component - this must come before resolution since target URIs may be inherited
        for (LogicalComponent<?> component : newComponents) {
            normalize(component, context);
        }

        // resolve services and references - evaluate all references since reinjection may apply
        for (LogicalComponent<?> component : domain.getComponents()) {
            promotionResolutionService.resolve(component, context);
            autowireInstantiator.instantiate(component, context);
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
                for (ResourceDefinition definition : included.getResources()) {
                    LogicalResource<?> resource = new LogicalResource<>(definition, domain);
                    resource.setDeployable(included.getName());
                    domain.addResource(resource);
                }
            }
        } else {
            for (ResourceDefinition definition : composite.getResources()) {
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
     * @return the newly instantiated domain-level components
     */
    private List<LogicalComponent<?>> instantiate(Composite composite, LogicalCompositeComponent domain, boolean synthetic, InstantiationContext context) {
        // instantiate the declared components
        Collection<ComponentDefinition<? extends Implementation<?>>> definitions = composite.getDeclaredComponents().values();
        List<LogicalComponent<?>> newComponents = new ArrayList<>(definitions.size());
        for (ComponentDefinition<? extends Implementation<?>> definition : definitions) {
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
        return newComponents;
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
    private LogicalComponent<?> instantiate(ComponentDefinition<?> definition, LogicalCompositeComponent parent, InstantiationContext context) {
        if (definition.getImplementation() instanceof CompositeImplementation) {
            ComponentDefinition<CompositeImplementation> componentDefinition = (ComponentDefinition<CompositeImplementation>) definition;
            return compositeComponentInstantiator.instantiate(componentDefinition, parent, context);
        } else {
            ComponentDefinition<Implementation<?>> componentDefinition = (ComponentDefinition<Implementation<?>>) definition;
            return atomicComponentInstantiator.instantiate(componentDefinition, parent, context);
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
            for (ComponentDefinition<? extends Implementation<?>> definition : include.getIncluded().getComponents().values()) {
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
     * Normalizes the component hierarchy by calculating autowire and promotion settings through a depth-first traversal of leaf/atomic components.
     *
     * @param component the component to normalize
     * @param context   the instantiation context
     */
    private void normalize(LogicalComponent<?> component, InstantiationContext context) {

        autowireNormalizer.normalize(component);

        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                normalize(child, context);
            }
        } else {
            promotionNormalizer.normalize(component, context);
        }
    }

    /**
     * Synthesizes a composite from a collection of composites using inclusion.
     * <p/>
     * A counter is maintained for each include name and used to generate include names in situations where they may clash, e.g. when two composites from
     * different contributions with the same name are depoyed together.
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
