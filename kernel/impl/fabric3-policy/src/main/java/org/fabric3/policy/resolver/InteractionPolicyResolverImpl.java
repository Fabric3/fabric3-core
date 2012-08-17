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
*/
package org.fabric3.policy.resolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;

/**
 * @version $Rev$ $Date$
 */
public class InteractionPolicyResolverImpl extends AbstractPolicyResolver implements InteractionPolicyResolver {

    public InteractionPolicyResolverImpl(@Reference PolicyRegistry policyRegistry,
                                         @Reference LogicalComponentManager lcm,
                                         @Reference PolicyEvaluator policyEvaluator) {
        super(policyRegistry, lcm, policyEvaluator);
    }

    public Set<Intent> resolveProvidedIntents(LogicalOperation operation, QName bindingType) throws PolicyResolutionException {
        Set<Intent> requiredIntents = getOperationIntents(operation);
        return filterProvidedIntents(bindingType, requiredIntents);
    }

    public Set<Intent> resolveProvidedIntents(LogicalBinding binding) throws PolicyResolutionException {
        Set<Intent> requiredIntents = aggregateBindingIntents(binding);
        QName type = binding.getDefinition().getType();
        return filterProvidedIntents(type, requiredIntents);
    }

    public Set<PolicySet> resolvePolicySets(LogicalBinding binding) throws PolicyResolutionException {
        QName type = binding.getDefinition().getType();
        BindingType bindingType = policyRegistry.getDefinition(type, BindingType.class);

        Set<QName> alwaysProvidedIntents = new LinkedHashSet<QName>();
        Set<QName> mayProvidedIntents = new LinkedHashSet<QName>();

        if (bindingType != null) {
            // tolerate a binding type not being registered
            alwaysProvidedIntents = bindingType.getAlwaysProvide();
            mayProvidedIntents = bindingType.getMayProvide();
        }

        // resolve policies against the binding
        Set<Intent> requiredIntents = aggregateBindingIntents(binding);
        Set<Intent> requiredIntentsCopy = new HashSet<Intent>(requiredIntents);
        // Remove intents that are provided
        for (Intent intent : requiredIntentsCopy) {
            QName intentName = intent.getName();
            if (alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        Set<PolicySet> policies = resolvePolicies(requiredIntents, binding);
        if (!requiredIntents.isEmpty()) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }

        Set<QName> policySets = aggregateBindingPolicySets(binding);
        for (QName name : policySets) {
            PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
            policies.add(policySet);
        }

        return policies;
    }

    public Set<PolicySet> resolvePolicySets(LogicalOperation operation, LogicalScaArtifact<?> artifact, QName type) throws PolicyResolutionException {
        BindingType bindingType = policyRegistry.getDefinition(type, BindingType.class);

        Set<QName> alwaysProvidedIntents = new LinkedHashSet<QName>();
        Set<QName> mayProvidedIntents = new LinkedHashSet<QName>();

        // FIXME This should not happen, all binding types should be registered
        if (bindingType != null) {
            alwaysProvidedIntents = bindingType.getAlwaysProvide();
            mayProvidedIntents = bindingType.getMayProvide();
        }

        Set<Intent> requiredIntents = getOperationIntents(operation);
        Set<Intent> requiredIntentsCopy = new HashSet<Intent>(requiredIntents);

        // Remove intents that are provided
        for (Intent intent : requiredIntentsCopy) {
            QName intentName = intent.getName();
            if (alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        Set<QName> policySets = getOperationPolicySets(operation);
        if (requiredIntents.isEmpty() && policySets.isEmpty()) {
            // short-circuit intent resolution
            return Collections.emptySet();
        }

        // resolve policies against the binding
        Set<PolicySet> policies = resolvePolicies(requiredIntents, artifact);
        if (!requiredIntents.isEmpty()) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }

        for (QName name : policySets) {
            PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
            policies.add(policySet);
        }

        return policies;

    }

    private Set<Intent> getOperationIntents(LogicalOperation operation) throws PolicyResolutionException {
        Set<QName> intentNames = new LinkedHashSet<QName>();
        intentNames.addAll(operation.getIntents());
        return expandAndFilterIntents(intentNames);
    }

    private Set<Intent> aggregateBindingIntents(LogicalBinding<?> logicalBinding) throws PolicyResolutionException {
        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new LinkedHashSet<QName>();
        intentNames.addAll(logicalBinding.getDefinition().getIntents());
        intentNames.addAll(aggregateIntents(logicalBinding));
        return expandAndFilterIntents(intentNames);
    }

    private Set<Intent> expandAndFilterIntents(Set<QName> intentNames) throws PolicyResolutionException {
        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);
        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        filterMutuallyExclusiveIntents(requiredIntents);
        return requiredIntents;
    }

    private Set<QName> getOperationPolicySets(LogicalOperation operation) {
        LogicalScaArtifact<?> temp = operation;
        Set<QName> policySetNames = new LinkedHashSet<QName>();
        while (temp != null) {
            policySetNames.addAll(temp.getPolicySets());
            temp = temp.getParent();
        }
        return policySetNames;
    }


    private Set<QName> aggregateBindingPolicySets(LogicalBinding<?> binding) {
        LogicalScaArtifact<?> temp = binding;
        Set<QName> policySetNames = new LinkedHashSet<QName>();
        while (temp != null) {
            policySetNames.addAll(temp.getPolicySets());
            temp = temp.getParent();
        }
        return policySetNames;
    }

    private Set<Intent> filterProvidedIntents(QName type, Set<Intent> requiredIntents) {
        BindingType bindingType = policyRegistry.getDefinition(type, BindingType.class);
        if (bindingType == null) {
            // tolerate a binding type not being registered
            return Collections.emptySet();
        }
        Set<QName> mayProvidedIntents = bindingType.getMayProvide();
        Set<Intent> intentsToBeProvided = new LinkedHashSet<Intent>();
        for (Intent intent : requiredIntents) {
            if (mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        return intentsToBeProvided;

    }

}
