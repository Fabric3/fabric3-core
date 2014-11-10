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
package org.fabric3.fabric.domain.generator.wire;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.contract.OperationNotFoundException;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.domain.generator.policy.PolicyResult;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class PhysicalOperationGeneratorImpl implements PhysicalOperationGenerator {
    private static final QName OASIS_ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName ALLOWS_BY_REFERENCE = new QName(org.fabric3.api.Namespaces.F3, "allowsPassByReference");
    private OperationResolver operationResolver;
    private GeneratorRegistry generatorRegistry;

    // Disables pass by value semantics by default. If this is disabled, pass-by-reference will be used for in-process invocations for
    // remotable services.
    private boolean passByValueEnabled = false;

    public PhysicalOperationGeneratorImpl(@Reference OperationResolver operationResolver, @Reference GeneratorRegistry generatorRegistry) {
        this.operationResolver = operationResolver;
        this.generatorRegistry = generatorRegistry;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:sca/@enableByValue")
    public void setPassByValueEnabled(boolean passByValueEnabled) {
        this.passByValueEnabled = passByValueEnabled;
    }

    public Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> operations, boolean remote, PolicyResult policyResult)
            throws GenerationException {

        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<>(operations.size());
        Set<PolicySet> endpointPolicySets;
        if (policyResult != null) {
            endpointPolicySets = policyResult.getInterceptedEndpointPolicySets();
        } else {
            endpointPolicySets = Collections.emptySet();
        }

        for (LogicalOperation operation : operations) {
            PhysicalOperationDefinition physicalOperation = generate(operation, remote);
            if (policyResult != null) {
                List<PolicySet> policies = policyResult.getInterceptedPolicySets(operation);
                List<PolicySet> allPolicies = new ArrayList<>(endpointPolicySets);
                for (PolicySet policy : policies) {
                    // strip duplicates from endpoint and operation policies 
                    if (!allPolicies.contains(policy)) {
                        allPolicies.add(policy);
                    }
                }
                PolicyMetadata metadata = policyResult.getMetadata(operation);
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptors(operation, allPolicies, metadata);
                physicalOperation.setInterceptors(interceptors);
            }
            physicalOperations.add(physicalOperation);
        }
        return physicalOperations;
    }

    public Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> sources,
                                                               List<LogicalOperation> targets,
                                                               boolean remote,
                                                               PolicyResult result) throws GenerationException {
        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<>(sources.size());
        Set<PolicySet> endpointPolicySets;
        if (result != null) {
            endpointPolicySets = result.getInterceptedEndpointPolicySets();
        } else {
            endpointPolicySets = Collections.emptySet();
        }

        for (LogicalOperation source : sources) {
            LogicalOperation target;
            try {
                target = operationResolver.resolve(source, targets);
            } catch (OperationNotFoundException e) {
                throw new GenerationException(e);
            }
            PhysicalOperationDefinition physicalOperation = generate(source, target, remote);
            if (result != null) {
                List<PolicySet> policies = result.getInterceptedPolicySets(source);
                List<PolicySet> allPolicies = new ArrayList<>(endpointPolicySets);
                allPolicies.addAll(policies);
                PolicyMetadata metadata = result.getMetadata(source);
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptors(source, allPolicies, metadata);
                physicalOperation.setInterceptors(interceptors);
            }
            physicalOperations.add(physicalOperation);
        }
        return physicalOperations;
    }

    /**
     * Generates interceptor definitions for the operation based on a set of resolved policies.
     *
     * @param operation the operation
     * @param policies  the policies
     * @param metadata  policy metadata
     * @return the interceptor definitions
     * @throws GenerationException if a generation error occurs
     */
    private Set<PhysicalInterceptorDefinition> generateInterceptors(LogicalOperation operation, List<PolicySet> policies, PolicyMetadata metadata)
            throws GenerationException {
        if (policies == null) {
            return Collections.emptySet();
        }
        Set<PhysicalInterceptorDefinition> interceptors = new LinkedHashSet<>();
        for (PolicySet policy : policies) {
            if (policy.getExpression() == null) {
                // empty policy
                continue;
            }
            QName expressionName = policy.getExpressionName();
            InterceptorGenerator generator = generatorRegistry.getInterceptorGenerator(expressionName);
            PhysicalInterceptorDefinition pid = generator.generate(policy.getExpression(), metadata, operation);
            if (pid != null) {
                URI contributionClassLoaderId = operation.getParent().getParent().getDefinition().getContributionUri();
                pid.setWireClassLoaderId(contributionClassLoaderId);
                pid.setPolicyClassLoaderId(policy.getContributionUri());
                interceptors.add(pid);
            }
        }
        return interceptors;
    }

    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are the same.
     *
     * @param source the logical operation to generate from
     * @param remote true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process wire.
     * @return the PhysicalOperationDefinition
     */
    private PhysicalOperationDefinition generate(LogicalOperation source, boolean remote) {
        Operation o = source.getDefinition();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        if (o.getIntents().contains(OASIS_ONEWAY)) {
            operation.setOneWay(true);
        }
        boolean remotable = o.isRemotable();
        operation.setRemotable(remotable);
        if (remotable && !useByReference(source, remote)) {
            operation.setAllowsPassByReference(false);
        }
        // the source and target in-, out- and fault types are the same since the source and target contracts are the same
        Class<?> returnType = o.getOutputType().getType();
        String returnName = returnType.getName();
        operation.setSourceReturnType(returnName);
        operation.setTargetReturnType(returnName);

        for (DataType fault : o.getFaultTypes()) {
            Class<?> faultType = fault.getType();
            String faultName = faultType.getName();
            operation.addSourceFaultType(faultName);
            operation.addTargetFaultType(faultName);
        }

        List<DataType> params = o.getInputTypes();
        for (DataType param : params) {
            Class<?> paramType = param.getType();
            String paramName = paramType.getName();
            operation.addSourceParameterType(paramName);
            operation.addTargetParameterType(paramName);
        }
        return operation;

    }

    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are different.
     *
     * @param source the source logical operation to generate from
     * @param target the target logical operations to generate from
     * @param remote true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process wire.
     * @return the PhysicalOperationDefinition
     */
    private PhysicalOperationDefinition generate(LogicalOperation source, LogicalOperation target, boolean remote) {
        Operation o = source.getDefinition();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        boolean remotable = o.isRemotable();
        operation.setRemotable(remotable);
        if (remotable && (!useByReference(source, remote) || !useByReference(target, remote))) {
            operation.setAllowsPassByReference(false);
        }

        if (o.getIntents().contains(OASIS_ONEWAY)) {
            operation.setOneWay(true);
        }
        Class<?> returnType = o.getOutputType().getType();
        operation.setSourceReturnType(returnType.getName());

        for (DataType fault : o.getFaultTypes()) {
            Class<?> faultType = fault.getType();
            operation.addSourceFaultType(faultType.getName());
        }

        List<DataType> params = o.getInputTypes();
        for (DataType param : params) {
            Class<?> paramType = param.getType();
            operation.addSourceParameterType(paramType.getName());
        }
        Operation targetDefinition = target.getDefinition();

        Class<?> targetReturnType = targetDefinition.getOutputType().getType();
        operation.setTargetReturnType(targetReturnType.getName());

        for (DataType targetFault : targetDefinition.getFaultTypes()) {
            Class<?> faultType = targetFault.getType();
            operation.addTargetFaultType(faultType.getName());
        }

        List<DataType> targetParams = targetDefinition.getInputTypes();
        for (DataType param : targetParams) {
            Class<?> paramType = param.getType();
            operation.addTargetParameterType(paramType.getName());
        }

        return operation;
    }

    private boolean useByReference(LogicalOperation operation, boolean remote) {
        if (!passByValueEnabled || remote) {
            // Pass-by-value is disabled or the invocation chain is remote. Pass-by-reference semantics should be used for remote invocation chains
            // since the binding will enforce pass-by-value implicitly through serialization
            return true;
        }
        LogicalAttachPoint logicalAttachPoint = operation.getParent();
        LogicalComponent<?> component = logicalAttachPoint.getParent();
        return operation.getIntents().contains(ALLOWS_BY_REFERENCE) || logicalAttachPoint.getIntents().contains(ALLOWS_BY_REFERENCE)
               || component.getIntents().contains(ALLOWS_BY_REFERENCE) || component.getDefinition().getImplementation().getIntents().contains(
                ALLOWS_BY_REFERENCE) || component.getDefinition().getImplementation().getComponentType().getIntents().contains(ALLOWS_BY_REFERENCE);
    }
}
