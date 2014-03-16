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
package org.fabric3.policy.resolver;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Base class for resolving policies.
 */
public class AbstractPolicyResolver {
    protected LogicalComponentManager lcm;
    protected PolicyEvaluator policyEvaluator;
    protected PolicyRegistry policyRegistry;

    protected AbstractPolicyResolver(PolicyRegistry policyRegistry, LogicalComponentManager lcm, PolicyEvaluator policyEvaluator) {
        this.policyRegistry = policyRegistry;
        this.lcm = lcm;
        this.policyEvaluator = policyEvaluator;
    }

    /**
     * Resolves intents to policies.
     *
     * @param intents the intents to resolve
     * @param target  the logical artifact to evaluate policy set "applies to" constraints against
     * @return the resolved policy sets
     * @throws PolicyResolutionException if there is an error during resolution
     */
    protected Set<PolicySet> resolvePolicies(Set<Intent> intents, LogicalScaArtifact<?> target) throws PolicyResolutionException {

        Set<PolicySet> policies = new LinkedHashSet<>();

        Collection<PolicySet> definitions = policyRegistry.getAllDefinitions(PolicySet.class);
        // Calculate appliesTo by first determining if the policy set provides the intent and then matching its appliesTo expression
        // against the logical artifact given as a target
        for (PolicySet policySet : definitions) {
            Iterator<Intent> iterator = intents.iterator();
            while (iterator.hasNext()) {
                Intent intent = iterator.next();
                if (policySet.doesProvide(intent.getName())) {
                    String appliesTo = policySet.getAppliesTo();
                    String attachTo = policySet.getAttachTo();
                    if ((appliesTo == null && attachTo == null) || (attachTo == null && policyEvaluator.doesApply(appliesTo, target)) || attachTo != null) {
                        policies.add(policySet);
                        iterator.remove();
                    }
                }
            }
        }
        return policies;
    }

    /**
     * Filter invalid intents.
     *
     * @param type            the type to filter on
     * @param requiredIntents the intents to filter
     * @throws PolicyResolutionException if an error is encountered filtering
     */
    protected void filterInvalidIntents(QName type, Set<Intent> requiredIntents) throws PolicyResolutionException {
        for (Iterator<Intent> it = requiredIntents.iterator(); it.hasNext(); ) {
            Intent intent = it.next();
            QName intentName = intent.getName();
            if (intent.getIntentType() != null) {
                if (!intent.doesConstrain(type)) {
                    it.remove();
                }
            } else {
                if (!intent.isQualified()) {
                    throw new PolicyResolutionException("Unqualified intent without constrained artifact: " + intentName);
                }
                Intent qualifiableIntent = policyRegistry.getDefinition(intent.getQualifiable(), Intent.class);
                if (qualifiableIntent == null) {
                    throw new PolicyResolutionException("Unknown intent: " + intent.getQualifiable());
                }
                if (!qualifiableIntent.doesConstrain(type)) {
                    it.remove();
                }
            }
        }

    }

    protected void filterMutuallyExclusiveIntents(Set<Intent> intents) {
        if (intents.isEmpty()) {
            return;
        }
        Set<Intent> removed = new HashSet<>();

        for (Intent current : intents) {
            if (removed.contains(current)) {
                continue;
            }
            for (Intent intent : intents) {
                if (current.getExcludes().contains(intent.getName()) || intent.getExcludes().contains(current.getName())) {

                    removed.add(intent);
                }
            }
        }
        for (Intent intent : removed) {
            intents.remove(intent);
        }
    }

    /**
     * Aggregates intents of a binding by walking the implementation and structural hierarchies of the parent.
     *
     * @param binding the binding
     * @return the aggregated intents
     */
    protected Set<QName> aggregateIntents(LogicalBinding<?> binding) throws PolicyResolutionException {
        Bindable parent = binding.getParent();
        Set<QName> aggregatedIntents = new LinkedHashSet<>();

        // add binding intents
        aggregatedIntents.addAll(binding.getIntents());

        validateIntents(binding);

        if (parent instanceof LogicalReference) {
            return aggregateReferenceIntents((LogicalReference) parent, aggregatedIntents);
        } else if (parent instanceof LogicalService) {
            return aggregateServiceIntents((LogicalService) parent, aggregatedIntents);
        } else {
            // channel
            LogicalChannel channel = (LogicalChannel) parent;
            aggregatedIntents.addAll(channel.getDefinition().getIntents());
            aggregatedIntents.addAll(channel.getIntents());
            return aggregatedIntents;
        }
    }

    private void validateIntents(LogicalBinding<?> binding) throws PolicyResolutionException {
        Set<Intent> intents = policyRegistry.getDefinitions(binding.getIntents(), Intent.class);
        for (Intent current : intents) {
            for (Intent intent : intents) {
                if (current.getExcludes().contains(intent.getName())) {
                    throw new PolicyResolutionException("Mutually exclusive intents specified on binding: " + binding.getParent().getUri());
                }
            }
        }

    }

    /**
     * Aggregates intents fof a service by walking the implementation and structural hierarchies of the parent.
     *
     * @param service the service
     * @return the aggregated intents
     */
    private Set<QName> aggregateServiceIntents(LogicalService service, Set<QName> aggregatedIntents) {
        processIntents(service.getIntents(), aggregatedIntents);

        // walk the implementation and structural hierarchy of the service
        LogicalComponent<?> parent = service.getParent();

        LogicalService currentService = service;

        while (parent != null) {
            processIntents(parent.getIntents(), aggregatedIntents);
            ComponentDefinition<? extends Implementation<?>> definition = parent.getDefinition();
            if (definition != null) {
                processIntents(definition.getIntents(), aggregatedIntents);
                Implementation<?> implementation = definition.getImplementation();
                if (implementation != null) {
                    processIntents(implementation.getIntents(), aggregatedIntents);
                    ComponentType componentType = implementation.getComponentType();
                    if (componentType != null) {
                        processIntents(componentType.getIntents(), aggregatedIntents);
                    }
                }
            }

            if (parent.getParent() != null && parent.getParent().getParent() == null) {
                // Special case - at the domain level the top level composite is removed during deployment. Its intents (defined in the component type) need
                // to be added to the hierarchy. This is achieved by walking the definitions.
                ComponentDefinition<?> componentDefinition = parent.getDefinition();
                ComponentType type = componentDefinition.getParent();
                processIntents(type.getIntents(), aggregatedIntents);
            }

            if (parent instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent compositeParent = (LogicalCompositeComponent) parent;
                for (LogicalService logicalService : compositeParent.getServices()) {
                    if (logicalService.getPromotedUri().equals(currentService.getUri())) {
                        processIntents(logicalService.getIntents(), aggregatedIntents);
                        currentService = logicalService;
                    }
                }
            }

            parent = parent.getParent();
        }
        return aggregatedIntents;
    }

    /**
     * Aggregates intents fof a reference by walking the implementation and structural hierarchies of the parent.
     *
     * @param reference the service
     * @return the aggregated intents
     */
    private Set<QName> aggregateReferenceIntents(LogicalReference reference, Set<QName> aggregatedIntents) {
        processIntents(reference.getIntents(), aggregatedIntents);

        // walk the implementation and structural hierarchy of the service
        LogicalComponent<?> parent = reference.getParent();

        LogicalReference currentReference = reference;

        while (parent != null) {
            processIntents(parent.getIntents(), aggregatedIntents);

            ComponentDefinition<? extends Implementation<?>> definition = parent.getDefinition();
            if (definition != null) {
                processIntents(definition.getIntents(), aggregatedIntents);
                Implementation<?> implementation = definition.getImplementation();
                if (implementation != null) {
                    processIntents(implementation.getIntents(), aggregatedIntents);
                    ComponentType componentType = implementation.getComponentType();
                    if (componentType != null) {
                        processIntents(componentType.getIntents(), aggregatedIntents);
                    }
                }
            }

            if (parent.getParent() != null && parent.getParent().getParent() == null) {
                // Special case - at the domain level the top level composite is removed during deployment. Its intents (defined in the component type) need
                // to be added to the hierarchy. This is achieved by walking the definitions.
                ComponentDefinition<?> componentDefinition = parent.getDefinition();
                ComponentType type = componentDefinition.getParent();
                processIntents(type.getIntents(), aggregatedIntents);
            }

            if (parent instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent compositeParent = (LogicalCompositeComponent) parent;
                for (LogicalReference logicalReference : compositeParent.getReferences()) {
                    for (URI promotedUri : logicalReference.getPromotedUris()) {
                        if (promotedUri.equals(currentReference.getUri())) {
                            processIntents(logicalReference.getIntents(), aggregatedIntents);
                            currentReference = logicalReference;
                        }
                    }
                }
            }

            parent = parent.getParent();
        }
        return aggregatedIntents;

    }

    /**
     * Aggregate intents from ancestors of a component. Components are handled differently than other SCA artifacts as implementation intents are inherited.
     *
     * @param component the logical artifact to aggregate intents for
     * @return the aggregated intents
     */
    protected Set<QName> aggregateIntents(LogicalComponent<?> component) {
        LogicalComponent<?> current = component;
        Set<QName> aggregatedIntents = new LinkedHashSet<>();

        while (current != null) {
            Set<QName> currentIntents = current.getIntents();
            processIntents(currentIntents, aggregatedIntents);

            ComponentDefinition<? extends Implementation<?>> definition = current.getDefinition();
            if (definition != null) {
                processIntents(definition.getIntents(), aggregatedIntents);
                Implementation<?> implementation = definition.getImplementation();
                if (implementation != null) {
                    processIntents(implementation.getIntents(), aggregatedIntents);
                    ComponentType componentType = implementation.getComponentType();
                    if (componentType != null) {
                        processIntents(componentType.getIntents(), aggregatedIntents);
                    }
                }
            }
            current = current.getParent();
        }
        return aggregatedIntents;
    }

    /**
     * Adds the contents of the intents to the set of aggregated intents according to rules set out in the SCA policy specification
     *
     * @param intents           the intents to add
     * @param aggregatedIntents the set of aggregated intents
     */
    private void processIntents(Set<QName> intents, Set<QName> aggregatedIntents) {
        for (QName currentIntent : intents) {
            String localPart = currentIntent.getLocalPart();
            boolean exclude = false;
            String namespace = currentIntent.getNamespaceURI();
            for (Iterator<QName> iterator = aggregatedIntents.iterator(); iterator.hasNext(); ) {
                QName aggregatedIntent = iterator.next();
                if (namespace.equals(aggregatedIntent.getNamespaceURI()) && aggregatedIntent.getLocalPart().startsWith(localPart + ".")) {
                    // if the parent intent is a profile intent of a qualified intent on the child element, ignore the profile intent
                    exclude = true;
                    break;
                } else if (namespace.equals(aggregatedIntent.getNamespaceURI()) && localPart.startsWith(aggregatedIntent.getLocalPart() + ".")) {
                    // if the intent from the parent qualifies a profile intent on a child element, remove the child profile intent and add the
                    // parent qualified intent to the aggregated intents
                    iterator.remove();
                    break;
                }
            }
            if (!exclude) {
                aggregatedIntents.add(currentIntent);
            }
        }
    }

    /**
     * Resolves intents, including expanding profile intents.
     *
     * @param intentNames the intent names to resolve
     * @return the expanded intents
     * @throws PolicyResolutionException if an exception is encountered resolving the intents
     */
    protected Set<Intent> resolveIntents(Set<QName> intentNames) throws PolicyResolutionException {
        Set<Intent> requiredIntents = new LinkedHashSet<>();
        for (QName intentName : intentNames) {
            Intent intent = policyRegistry.getDefinition(intentName, Intent.class);
            if (intent == null) {
                throw new PolicyResolutionException("Unknown intent: " + intentName);
            }
            if (intent.isProfile()) {
                for (QName requiredIntentName : intent.getRequires()) {
                    Intent requiredIntent = policyRegistry.getDefinition(requiredIntentName, Intent.class);
                    if (requiredIntent == null) {
                        throw new PolicyResolutionException("Unknown intent" + requiredIntentName);
                    }
                    requiredIntents.add(requiredIntent);

                }
            } else {
                requiredIntents.add(intent);
            }
        }
        return requiredIntents;
    }

}
