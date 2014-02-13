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
package org.fabric3.fabric.deployment.instantiator.promotion;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.fabric.deployment.instantiator.PromotionNormalizer;
import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.Autowire;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.spi.deployment.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the PromotionNormalizer.
 * <p/>
 * The service promotion normalization algorithm works as follows: <li>A reverse-ordered list of services is constructed by walking the service
 * promotion hierarchy from a leaf component to the domain component. The leaf service is added as the last list entry.
 * <p/>
 * <li>The list is iterated in order, starting with the service nearest the domain level.
 * <p/>
 * <li>For each entry, bindings are added or replaced (according to the override setting for the service), policies added, a service contract set if
 * not defined, and the leaf component set as the leaf parent. <li>
 * <p/>
 * </ul> The reference promotion algorithm works as follows: <li> A reverse-ordered list of references is constructed by walking the reference
 * promotion hierarchy from a leaf component to the domain component. The leaf reference is added as the last list entry.
 * <p/>
 * <li>The list is iterated in order, starting with the reference nearest the domain level.
 * <p/>
 * <li>For each entry, bindings are added or replaced (according to the override setting for the reference), policies added and a service contract set
 * if not defined
 * <p/>
 * <li>The list is iterated a second time and wires for references are examined with their targets pushed down to the next (child) level in the
 * hierarchy.
 */
public class PromotionNormalizerImpl implements PromotionNormalizer {
    private PolicyRegistry registry;

    /**
     * Bootstrap constructor.
     */
    public PromotionNormalizerImpl() {
    }

    @Constructor
    public PromotionNormalizerImpl(@Reference PolicyRegistry registry) {
        this.registry = registry;
    }

    public void normalize(LogicalComponent<?> component, InstantiationContext context) {
        normalizeServicePromotions(component, context);
        normalizeReferenceAndWirePromotions(component, context);
    }

    private void normalizeServicePromotions(LogicalComponent<?> component, InstantiationContext context) {
        for (LogicalService service : component.getServices()) {
            LinkedList<LogicalService> services = new LinkedList<>();
            // add the leaf service as the last element
            services.add(service);
            getPromotionHierarchy(service, services);
            if (services.isEmpty()) {
                continue;
            }
            processServicePromotions(services, context);
        }
    }

    private void normalizeReferenceAndWirePromotions(LogicalComponent<?> component, InstantiationContext context) {
        for (LogicalReference reference : component.getReferences()) {
            LinkedList<LogicalReference> references = new LinkedList<>();
            // add the leaf (promoted) reference as the last element
            references.add(reference);
            getPromotionHierarchy(reference, references);
            if (references.isEmpty()) {
                continue;
            }
            processReferencePromotions(references, context);
            processWirePromotions(references, context);
        }
    }

    /**
     * Processes the service promotion hierarchy by updating bindings, policies, and the service contract.
     *
     * @param services the sorted service promotion hierarchy
     * @param context  the instantiation  context
     */
    private void processServicePromotions(LinkedList<LogicalService> services, InstantiationContext context) {
        if (services.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        LogicalService leafService = services.getLast();
        LogicalComponent<?> leafComponent = leafService.getParent();
        List<LogicalBinding<?>> bindings = new ArrayList<>();
        List<LogicalBinding<?>> callbackBindings = new ArrayList<>();
        Set<QName> intents = new HashSet<>();
        Set<QName> policySets = new HashSet<>();

        for (LogicalService service : services) {
            // TODO determine if bindings should be overriden - for now, override
            if (service.getBindings().isEmpty()) {
                service.overrideBindings(bindings);
                service.overrideCallbackBindings(callbackBindings);
            } else {
                bindings = new ArrayList<>();
                bindings.addAll(service.getBindings());
                callbackBindings = new ArrayList<>();
                callbackBindings.addAll(service.getCallbackBindings());
            }
            if (service.getIntents().isEmpty()) {
                service.addIntents(intents);
            } else {
                intents = new HashSet<>();
                intents.addAll(service.getIntents());
            }
            if (service.getPolicySets().isEmpty()) {
                service.addPolicySets(policySets);
            } else {
                policySets = new HashSet<>();
                policySets.addAll(service.getPolicySets());
            }
            validateIntents(service, context);
            service.setLeafComponent(leafComponent);
            service.setLeafService(leafService);
        }

    }

    /**
     * Processes the reference promotion hierarchy by updating bindings, policies, and the service contract.
     *
     * @param references the sorted reference promotion hierarchy
     * @param context    the instantiation  context
     */
    @SuppressWarnings({"unchecked"})
    private void processReferencePromotions(LinkedList<LogicalReference> references, InstantiationContext context) {
        if (references.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        LogicalReference leafReference = references.getLast();
        List<LogicalBinding<?>> bindings = new ArrayList<>();
        Set<QName> intents = new HashSet<>();
        Set<QName> policySets = new HashSet<>();
        Autowire autowire = Autowire.INHERITED;

        for (LogicalReference reference : references) {
            AbstractReference referenceDefinition = reference.getDefinition();
            if (referenceDefinition.getAutowire() == Autowire.INHERITED) {
                reference.setAutowire(autowire);
            } else {
                autowire = referenceDefinition.getAutowire();
            }
            // TODO determine if bindings should be overriden - for now, override
            if (reference.getBindings().isEmpty()) {
                List<LogicalBinding<?>> newBindings = new ArrayList<>();

                for (LogicalBinding<?> binding : bindings) {
                    // create a new logical binding based on the promoted one
                    BindingDefinition definition = binding.getDefinition();
                    QName deployable = binding.getDeployable();
                    LogicalBinding<?> newBinding = new LogicalBinding(definition, reference, deployable);
                    newBindings.add(newBinding);
                }
                reference.overrideBindings(newBindings);
            } else {
                bindings = new ArrayList<>();
                bindings.addAll(reference.getBindings());
            }
            if (reference.getIntents().isEmpty()) {
                reference.addIntents(intents);
            } else {
                intents = new HashSet<>();
                intents.addAll(reference.getIntents());
            }
            if (reference.getPolicySets().isEmpty()) {
                reference.addPolicySets(policySets);
            } else {
                policySets = new HashSet<>();
                policySets.addAll(reference.getPolicySets());
            }
            reference.setLeafReference(leafReference);
        }
        validateIntents(leafReference, context);
    }

    private void validateIntents(Bindable bindable, InstantiationContext context) {
        if (registry == null) {
            // don't validate intents during bootstrap
            return;
        }
        Set<QName> intents = bindable.getIntents();
        if (intents.isEmpty()) {
            return;
        }
        Set<Intent> resolved = registry.getDefinitions(intents, Intent.class);

        // check for mutually exclusive intents
        for (Intent intent : resolved) {
            for (QName exclude : intent.getExcludes()) {
                if (intents.contains(exclude)) {
                    String prefix = bindable instanceof LogicalReference ? "Reference " : "Service ";
                    MutuallyExclusiveIntents error = new MutuallyExclusiveIntents(prefix + bindable.getUri()
                                                                                          + " is configured with mutually exclusive intents: "
                                                                                          + intent.getName() + "," + exclude, bindable);
                    context.addError(error);
                }
            }
        }
    }

    /**
     * Processes the wiring hierarchy by pushing wires down to child components.
     *
     * @param references the sorted reference promotion hierarchy
     * @param context    the instantiation context
     */
    // TODO handle wire addition
    private void processWirePromotions(LinkedList<LogicalReference> references, InstantiationContext context) {
        if (references.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        List<LogicalService> newTargets = new ArrayList<>();

        for (LogicalReference reference : references) {
            LogicalCompositeComponent composite = reference.getParent().getParent();
            for (LogicalWire wire : reference.getWires()) {
                // TODO support wire overrides
                LogicalService target = wire.getTarget();
                newTargets.add(target);
            }
            if (!newTargets.isEmpty()) {
                List<LogicalWire> newWires = new ArrayList<>();
                for (LogicalService target : newTargets) {
                    QName deployable = composite.getDeployable();
                    LogicalWire newWire = new LogicalWire(reference.getParent(), reference, target, deployable);
                    newWires.add(newWire);
                }
                composite.overrideWires(reference, newWires);
                // TODO if override, new targets should be erased
//                newTargets = new ArrayList<LogicalService>();
            }
            if (!validateMultiplicity(reference, newTargets, context)) {
                return;
            }
        }

    }

    /**
     * Validates that the reference multiplicity is not violated by reference targets inherited through a promotion hierarchy.
     *
     * @param reference the reference to validate
     * @param targets   the targets specified in the promotion hierarchy
     * @param context   the context
     * @return true if the validation was successful
     */
    private boolean validateMultiplicity(LogicalReference reference, List<LogicalService> targets, InstantiationContext context) {
        if (reference.getParent().getAutowire() == Autowire.ON
                || !reference.getBindings().isEmpty()
                || reference.getAutowire() == Autowire.ON
                || reference.getComponentReference() != null) {     // Reference should not be configured in the component.
            // If it is (i.e. getComponentReference() != null, avoid check and return true.
            return true;
        }
        Multiplicity multiplicity = reference.getDefinition().getMultiplicity();
        switch (multiplicity) {
        case ONE_N:
            if (targets.size() < 1) {
                URI referenceName = reference.getUri();
                InvalidNumberOfTargets error =
                        new InvalidNumberOfTargets("At least one target must be configured for reference: " + referenceName, reference);
                context.addError(error);
                return false;
            }
            return true;
        case ONE_ONE:
            if (targets.size() < 1) {
                URI referenceName = reference.getUri();
                InvalidNumberOfTargets error = new InvalidNumberOfTargets("At least one target must be configured for reference "
                                                                                  + "(no targets configured): " + referenceName, reference);
                context.addError(error);
                return false;
            } else if (targets.size() > 1) {
                URI referenceName = reference.getUri();
                InvalidNumberOfTargets error = new InvalidNumberOfTargets("Only one target must be configured for reference "
                                                                                  + "(multiple targets configured via promotions): "
                                                                                  + referenceName, reference);
                context.addError(error);
                return false;
            }
            return true;

        case ZERO_N:
            return true;
        case ZERO_ONE:
            if (targets.size() > 1) {
                URI referenceName = reference.getUri();
                InvalidNumberOfTargets error = new InvalidNumberOfTargets("At most one target must be configured for reference "
                                                                                  + "(multiple targets configured via promotions): "
                                                                                  + referenceName, reference);
                context.addError(error);
                return false;
            }
            return true;
        }
        return true;
    }


    /**
     * Updates the list of services with the promotion hierarchy for the given service. The list is populated in reverse order so that the leaf
     * (promoted) service is stored last.
     *
     * @param service  the current service to ascend from
     * @param services the list
     */
    private void getPromotionHierarchy(LogicalService service, LinkedList<LogicalService> services) {
        LogicalComponent<CompositeImplementation> parent = service.getParent().getParent();
        URI serviceUri = service.getUri();
        for (LogicalService promotion : parent.getServices()) {
            URI targetUri = promotion.getPromotedUri();
            if (targetUri.getFragment() == null) {
                // no service specified
                if (targetUri.equals(UriHelper.getDefragmentedName(serviceUri))) {
                    services.addFirst(promotion);
                    if (parent.getParent() != null) {
                        getPromotionHierarchy(promotion, services);
                    }
                }

            } else {
                if (targetUri.equals(serviceUri)) {
                    services.addFirst(promotion);
                    if (parent.getParent() != null) {
                        getPromotionHierarchy(promotion, services);
                    }
                }
            }
        }
    }

    /**
     * Updates the list of references with the promotion hierarchy for the given reference. The list is populated in reverse order so that the leaf
     * (promoted) reference is stored last.
     *
     * @param reference  the current service to ascend from
     * @param references the list
     */
    private void getPromotionHierarchy(LogicalReference reference, LinkedList<LogicalReference> references) {
        URI referenceUri = reference.getUri();
        LogicalComponent<CompositeImplementation> parent = reference.getParent().getParent();
        for (LogicalReference promotion : parent.getReferences()) {
            List<URI> promotedUris = promotion.getPromotedUris();
            for (URI promotedUri : promotedUris) {
                if (promotedUri.getFragment() == null) {
                    if (promotedUri.equals(UriHelper.getDefragmentedName(referenceUri))) {
                        references.addFirst(promotion);
                        if (parent.getParent() != null) {
                            getPromotionHierarchy(promotion, references);
                        }
                    }
                } else {
                    if (promotedUri.equals(referenceUri)) {
                        references.addFirst(promotion);
                        if (parent.getParent() != null) {
                            getPromotionHierarchy(promotion, references);
                        }
                    }
                }
            }

        }
    }

}
