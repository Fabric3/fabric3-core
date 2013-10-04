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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class InteractionPolicyResolverImpl extends AbstractPolicyResolver implements InteractionPolicyResolver {
    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName LOCAL_MANAGED_TRANSACTION = new QName(Constants.SCA_NS, "managedTransaction.local");
    private static final QName NO_MANAGED_TRANSACTION = new QName(Constants.SCA_NS, "noManagedTransaction");
    private static final QName PROPAGATES_TRANSACTION = new QName(Constants.SCA_NS, "propagatesTransaction");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName ASYNC_INVOCATION = new QName(Constants.SCA_NS, "asyncInvocation");
    private static final QName NO_LISTENER = new QName(Constants.SCA_NS, "noListener");

    public InteractionPolicyResolverImpl(@Reference PolicyRegistry policyRegistry,
                                         @Reference LogicalComponentManager lcm,
                                         @Reference PolicyEvaluator policyEvaluator) {
        super(policyRegistry, lcm, policyEvaluator);
    }

    public IntentPair resolveIntents(LogicalOperation operation, QName bindingType) throws PolicyResolutionException {
        Set<Intent> requiredIntents = aggregateOperationIntents(operation);
        Set<Intent> providedIntents = filterProvidedIntents(bindingType, requiredIntents);
        return new IntentPair(requiredIntents, providedIntents);
    }

    public IntentPair resolveIntents(LogicalBinding binding) throws PolicyResolutionException {
        Set<Intent> requiredIntents = aggregateBindingIntents(binding);

        QName type = binding.getDefinition().getType();
        Set<Intent> providedIntents = filterProvidedIntents(type, requiredIntents);
        return new IntentPair(requiredIntents, providedIntents);
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
            throw new IntentResolutionException("Unable to resolve all intents", requiredIntents);
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

        Set<Intent> requiredIntents = aggregateOperationIntents(operation);
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
            throw new IntentResolutionException("Unable to resolve all intents", requiredIntents);
        }

        for (QName name : policySets) {
            PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
            policies.add(policySet);
        }

        return policies;

    }

    private Set<Intent> aggregateOperationIntents(LogicalOperation operation) throws PolicyResolutionException {
        Set<QName> intentNames = new LinkedHashSet<QName>();
        intentNames.addAll(operation.getIntents());
        return expandAndFilterIntents(intentNames, operation);
    }

    private Set<Intent> aggregateBindingIntents(LogicalBinding<?> binding) throws PolicyResolutionException {
        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = aggregateIntents(binding);
        return expandAndFilterIntents(intentNames, binding);
    }

    private Set<Intent> expandAndFilterIntents(Set<QName> intentNames, LogicalScaArtifact artifact) throws PolicyResolutionException {
        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveIntents(intentNames);

        validateIntents(requiredIntents, artifact);
        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        filterMutuallyExclusiveIntents(requiredIntents);
        return requiredIntents;
    }

    /**
     * Validates intent combinations per the SCA policy spec where the intents do not specify this in their excludes configuration. This specifically deals with
     * implementation and messaging transaction policies and results in the hardcoded hack below.
     *
     * @param intents  the intents to validate.
     * @param artifact the artifact to which the intents apply
     */
    private void validateIntents(Set<Intent> intents, LogicalScaArtifact artifact) throws PolicyResolutionException {
        if (intents.isEmpty()) {
            return;
        }
        boolean isTransactedOneWay = false;
        boolean isImmediateOneWay = false;
        boolean isNoManagedTransaction = false;
        boolean isLocalTransaction = false;
        boolean propagatesTransaction = false;
        boolean asyncInvocation = false;

        for (Intent intent : intents) {
            if (!isTransactedOneWay) {
                if (isTransactedOneWay = TRANSACTED_ONEWAY.equals(intent.getName())) {
                    continue;
                }
            }
            if (!isImmediateOneWay) {
                if (isImmediateOneWay = IMMEDIATE_ONEWAY.equals(intent.getName())) {
                    continue;
                }
            }
            if (!isNoManagedTransaction) {
                if (isNoManagedTransaction = NO_MANAGED_TRANSACTION.equals(intent.getName())) {
                    continue;
                }
            }
            if (!isLocalTransaction) {
                if (isLocalTransaction = LOCAL_MANAGED_TRANSACTION.equals(intent.getName())) {
                    continue;
                }
            }
            if (!asyncInvocation) {
                if (asyncInvocation = ASYNC_INVOCATION.equals(intent.getName())) {
                    continue;
                }
            }
            if (!propagatesTransaction) {
                if (propagatesTransaction = PROPAGATES_TRANSACTION.equals(intent.getName())) {
                    continue;
                }
            }
            if (NO_LISTENER.equals(intent.getName())) {
                if (!(artifact.getParent() instanceof LogicalReference)) {
                    throw new PolicyResolutionException("The noListener intent can only be specified on a reference");
                }
            }
        }
        if (isNoManagedTransaction && isTransactedOneWay) {
            throw new PolicyResolutionException("Cannot specify a one-way interaction on a component that is configured for no transaction context");
        } else if (isNoManagedTransaction && propagatesTransaction) {
            throw new PolicyResolutionException("Cannot specify propagates transaction on a component that is configured for no transaction context");
        } else if (isLocalTransaction && isTransactedOneWay) {
            throw new PolicyResolutionException("Cannot specify a transacted one-way interaction on a component that is configured for a local transaction");
        } else if (isLocalTransaction && propagatesTransaction) {
            throw new PolicyResolutionException("Cannot specify propagates transaction on a component that is configured for a local transaction context");
        }
        if (asyncInvocation && propagatesTransaction) {
            throw new PolicyResolutionException("Cannot specify propagates transaction on an async invocation");
        }
        if (isTransactedOneWay) {
            if (artifact instanceof LogicalBinding) {
                LogicalBinding<?> binding = (LogicalBinding) artifact;
                Bindable parent = binding.getParent();
                List<LogicalOperation> operations = parent.getOperations();
                for (LogicalOperation operation : operations) {
                    if (!operation.getIntents().contains(ONEWAY)) {
                        throw new PolicyResolutionException("Cannot specify transacted one-way for a request-response operation: " + parent.getUri());
                    }
                }
            } else if (artifact instanceof LogicalOperation) {
                LogicalOperation operation = (LogicalOperation) artifact;
                if (!operation.getIntents().contains(ONEWAY)) {
                    throw new PolicyResolutionException(
                            "Cannot specify transacted one-way for a request-response operation: " + operation.getParent().getUri());
                }
            }
        }
        if (isImmediateOneWay) {
            if (artifact instanceof LogicalBinding) {
                LogicalBinding<?> binding = (LogicalBinding) artifact;
                Bindable parent = binding.getParent();
                List<LogicalOperation> operations = parent.getOperations();
                for (LogicalOperation operation : operations) {
                    if (!operation.getIntents().contains(ONEWAY)) {
                        throw new PolicyResolutionException("Cannot specify immediate one-way for a request-response operation: " + parent.getUri());
                    }
                }
            } else if (artifact instanceof LogicalOperation) {
                LogicalOperation operation = (LogicalOperation) artifact;
                if (!operation.getIntents().contains(ONEWAY)) {
                    throw new PolicyResolutionException("Cannot specify immediate one-way for a request-response operation: " + operation.getParent().getUri());
                }
            }

        }
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
