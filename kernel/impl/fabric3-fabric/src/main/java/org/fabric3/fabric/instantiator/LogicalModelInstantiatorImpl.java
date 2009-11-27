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
*/
package org.fabric3.fabric.instantiator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.component.Property;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
 */
public class LogicalModelInstantiatorImpl implements LogicalModelInstantiator {
    /**
     * Represents a synthetic composite. Synthetic composites are created to instantiate multiple deployable composites in a single operation.
     */
    private QName SYNTHENTIC_COMPOSITE = new QName(Namespaces.IMPLEMENTATION, "SyntheticComposite");

    private final ResolutionService resolutionService;
    private final PromotionNormalizer promotionNormalizer;
    private final LogicalComponentManager logicalComponentManager;
    private final ComponentInstantiator atomicComponentInstantiator;
    private final ComponentInstantiator compositeComponentInstantiator;
    private WireInstantiator wireInstantiator;


    public LogicalModelInstantiatorImpl(@Reference ResolutionService resolutionService,
                                        @Reference PromotionNormalizer promotionNormalizer,
                                        @Reference LogicalComponentManager logicalComponentManager,
                                        @Reference(name = "atomicComponentInstantiator") ComponentInstantiator atomicComponentInstantiator,
                                        @Reference(name = "compositeComponentInstantiator") ComponentInstantiator compositeComponentInstantiator,
                                        @Reference WireInstantiator wireInstantiator) {
        this.resolutionService = resolutionService;
        this.promotionNormalizer = promotionNormalizer;
        this.logicalComponentManager = logicalComponentManager;
        this.atomicComponentInstantiator = atomicComponentInstantiator;
        this.compositeComponentInstantiator = compositeComponentInstantiator;
        this.wireInstantiator = wireInstantiator;
    }

    @SuppressWarnings("unchecked")
    public InstantiationContext include(LogicalCompositeComponent targetComposite, Composite composite) {
        return include(targetComposite, composite, false);
    }

    public InstantiationContext include(LogicalCompositeComponent targetComposite, List<Composite> composites) {
        Composite composite = synthesizeComposite(composites);
        return include(targetComposite, composite, true);
    }

    private InstantiationContext include(LogicalCompositeComponent targetComposite, Composite composite, boolean synthetic) {

        InstantiationContext context = new InstantiationContext(targetComposite);

        // merge the property values into the parent
        Map<String, Document> properties = includeProperties(composite, context);

        // instantiate all the components in the composite and add them to the parent
        List<LogicalComponent<?>> newComponents = instantiateComponents(properties, composite, synthetic, context);
        List<LogicalService> services = instantiateServices(composite, context);
        List<LogicalReference> references = instantiateReferences(composite, context);

        // explicit wires must be instantiated after the services have been merged
        wireInstantiator.instantiateWires(composite, context.getParent(), context);

        // resolve services and references
        resolve(targetComposite.getComponents(), services, references, context);

        // normalize bindings for each new component
        for (LogicalComponent<?> component : newComponents) {
            normalize(component);
        }
        return context;
    }


    private Map<String, Document> includeProperties(Composite composite, InstantiationContext context) {
        LogicalCompositeComponent parent = context.getParent();
        for (Property property : composite.getProperties().values()) {
            String name = property.getName();
            if (parent.getPropertyValues().containsKey(name)) {
                DuplicateProperty error = new DuplicateProperty(name, parent.getUri(), parent.getDefinition().getContributionUri());
                context.addError(error);
            } else {
                parent.setPropertyValue(name, property.getDefaultValue());
            }
        }
        return parent.getPropertyValues();
    }

    private List<LogicalComponent<?>> instantiateComponents(Map<String, Document> properties,
                                                            Composite composite,
                                                            boolean synthetic,
                                                            InstantiationContext context) {
        LogicalCompositeComponent parent = context.getParent();
        Collection<ComponentDefinition<? extends Implementation<?>>> definitions = composite.getDeclaredComponents().values();
        List<LogicalComponent<?>> newComponents = new ArrayList<LogicalComponent<?>>(definitions.size());
        for (ComponentDefinition<? extends Implementation<?>> definition : definitions) {
            LogicalComponent<?> logicalComponent = instantiate(parent, properties, definition, context);
            setAutowire(composite, definition, logicalComponent);
            setDeployable(logicalComponent, composite.getName());
            newComponents.add(logicalComponent);
            parent.addComponent(logicalComponent);
        }
        for (Include include : composite.getIncludes().values()) {
            // xcv FIXME need to recurse down included hierarchy
            for (ComponentDefinition<? extends Implementation<?>> definition : include.getIncluded().getComponents().values()) {
                LogicalComponent<?> logicalComponent = instantiate(parent, properties, definition, context);
                setAutowire(composite, definition, logicalComponent);
                if (synthetic) {
                    // If it is a synthetic composite, included composites are the deployables.
                    // Synthetic composites are used to deploy multiple composites as a group. They include the composites (deployables).
                    // Adding the deployable name to domain-level components allows them to be managed as a group after they are deployed.
                    setDeployable(logicalComponent, include.getIncluded().getName());
                } else {
                    setDeployable(logicalComponent, composite.getName());
                }
                newComponents.add(logicalComponent);
                parent.addComponent(logicalComponent);
            }
        }
        return newComponents;
    }

    @SuppressWarnings("unchecked")
    private LogicalComponent<?> instantiate(LogicalCompositeComponent parent,
                                            Map<String, Document> properties,
                                            ComponentDefinition<?> definition,
                                            InstantiationContext context) {

        if (definition.getImplementation().isComposite()) {
            ComponentDefinition<CompositeImplementation> componentDefinition = (ComponentDefinition<CompositeImplementation>) definition;
            return compositeComponentInstantiator.instantiate(parent, properties, componentDefinition, context);
        } else {
            ComponentDefinition<Implementation<?>> componentDefinition = (ComponentDefinition<Implementation<?>>) definition;
            return atomicComponentInstantiator.instantiate(parent, properties, componentDefinition, context);
        }
    }

    private List<LogicalService> instantiateServices(Composite composite, InstantiationContext context) {
        LogicalCompositeComponent parent = context.getParent();
        String base = parent.getUri().toString();
        List<LogicalService> services = new ArrayList<LogicalService>();
        // merge the composite service declarations into the parent
        for (CompositeService compositeService : composite.getServices().values()) {
            URI serviceURI = URI.create(base + '#' + compositeService.getName());
            URI promotedURI = compositeService.getPromote();
            LogicalService logicalService = new LogicalService(serviceURI, compositeService, parent);
            logicalService.setPromotedUri(URI.create(base + "/" + promotedURI));
            for (BindingDefinition binding : compositeService.getBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addBinding(logicalBinding);
            }
            for (BindingDefinition binding : compositeService.getCallbackBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addCallbackBinding(logicalBinding);
            }
            services.add(logicalService);
            parent.addService(logicalService);
        }
        return services;

    }

    private List<LogicalReference> instantiateReferences(Composite composite, InstantiationContext context) {
        LogicalCompositeComponent parent = context.getParent();
        String base = parent.getUri().toString();
        // merge the composite reference definitions into the parent
        List<LogicalReference> references = new ArrayList<LogicalReference>(composite.getReferences().size());
        for (CompositeReference compositeReference : composite.getReferences().values()) {
            URI referenceURi = URI.create(base + '#' + compositeReference.getName());
            LogicalReference logicalReference = new LogicalReference(referenceURi, compositeReference, parent);
            for (URI promotedUri : compositeReference.getPromotedUris()) {
                URI resolvedUri = URI.create(base + "/" + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }
            for (BindingDefinition binding : compositeReference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            for (BindingDefinition binding : compositeReference.getCallbackBindings()) {
                logicalReference.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            references.add(logicalReference);
            parent.addReference(logicalReference);
        }
        return references;

    }


    private void resolve(Collection<LogicalComponent<?>> components,
                         List<LogicalService> services,
                         List<LogicalReference> references,
                         InstantiationContext context) {

        // resolve composite services merged into the domain
        for (LogicalService service : services) {
            resolutionService.resolve(service, context);
        }

        // resove composite references merged into the domain
        for (LogicalReference reference : references) {
            resolutionService.resolve(reference, logicalComponentManager.getRootComponent(), context);
        }

        // resolve wires for each new component
        for (LogicalComponent<?> component : components) {
            resolutionService.resolve(component, context);
        }

    }

    /**
     * Normalizes the component and any children
     *
     * @param component the component to normalize
     */
    private void normalize(LogicalComponent<?> component) {
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                normalize(child);
            }
        } else {
            promotionNormalizer.normalize(component);
        }

    }

    /**
     * Synthesizes a composite from a collection of composites using inclusion.
     *
     * @param composites the composites to synthesize
     * @return the synthesized composite
     */
    private Composite synthesizeComposite(List<Composite> composites) {
        Composite synthesized = new Composite(SYNTHENTIC_COMPOSITE);
        for (Composite composite : composites) {
            Include include = new Include();
            include.setName(composite.getName());
            include.setIncluded(composite);
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
        }
        component.setDeployable(deployable);
    }

    private void setAutowire(Composite composite, ComponentDefinition<? extends Implementation<?>> definition, LogicalComponent<?> logicalComponent) {
        // use autowire settings on the original composite as an override if they are not specified on the component
        Autowire autowire;
        if (definition.getAutowire() == Autowire.INHERITED) {
            autowire = composite.getAutowire();
        } else {
            autowire = definition.getAutowire();
        }
        if (autowire == Autowire.ON || autowire == Autowire.OFF) {
            logicalComponent.setAutowireOverride(autowire);
        }
    }


}
