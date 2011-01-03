/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.contract.OperationNotFoundException;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.InterceptorGenerator;
import org.fabric3.spi.generator.PolicyMetadata;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * @version $Rev$ $Date$
 */
public class PhysicalOperationGeneratorImpl implements PhysicalOperationGenerator {
    private static final QName OASIS_ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName ALLOWS_BY_REFERENCE = new QName(Namespaces.F3, "allowsPassByReference");
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
    public void setPassByValueEnabled(boolean passByValueEnabled) {
        this.passByValueEnabled = passByValueEnabled;
    }

    public Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> operations, boolean remote, PolicyResult policyResult)
            throws GenerationException {

        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<PhysicalOperationDefinition>(operations.size());
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
                List<PolicySet> allPolicies = new ArrayList<PolicySet>(endpointPolicySets);
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
        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<PhysicalOperationDefinition>(sources.size());
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
                List<PolicySet> allPolicies = new ArrayList<PolicySet>(endpointPolicySets);
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
        Set<PhysicalInterceptorDefinition> interceptors = new LinkedHashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
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
     * @param remote true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process
     *               wire.
     * @return the PhysicalOperationDefinition
     */
    private PhysicalOperationDefinition generate(LogicalOperation source, boolean remote) {
        Operation o = source.getDefinition();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        operation.setEndsConversation(o.getConversationSequence() == Operation.CONVERSATION_END);
        if (o.getIntents().contains(OASIS_ONEWAY)) {
            operation.setOneWay(true);
        }
        boolean remotable = o.isRemotable();
        operation.setRemotable(remotable);
        if (remotable && !useByReference(source, remote)) {
            operation.setAllowsPassByReference(false);
        }
        // the source and target in-, out- and fault types are the same since the source and target contracts are the same
        Class<?> returnType = o.getOutputType().getPhysical();
        String returnName = returnType.getName();
        operation.setSourceReturnType(returnName);
        operation.setTargetReturnType(returnName);

        for (DataType<?> fault : o.getFaultTypes()) {
            Class<?> faultType = fault.getPhysical();
            String faultName = faultType.getName();
            operation.addSourceFaultType(faultName);
            operation.addTargetFaultType(faultName);
        }

        List<DataType<?>> params = o.getInputTypes();
        for (DataType<?> param : params) {
            Class<?> paramType = param.getPhysical();
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
     * @param remote true if the interceptor chain handles remote invocations - i.e. it is for a bound service, bound reference or inter-process
     *               wire.
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

        operation.setEndsConversation(o.getConversationSequence() == Operation.CONVERSATION_END);
        if (o.getIntents().contains(OASIS_ONEWAY)) {
            operation.setOneWay(true);
        }
        Class<?> returnType = o.getOutputType().getPhysical();
        operation.setSourceReturnType(returnType.getName());

        for (DataType<?> fault : o.getFaultTypes()) {
            Class<?> faultType = fault.getPhysical();
            operation.addSourceFaultType(faultType.getName());
        }

        List<DataType<?>> params = o.getInputTypes();
        for (DataType<?> param : params) {
            Class<?> paramType = param.getPhysical();
            operation.addSourceParameterType(paramType.getName());
        }
        Operation targetDefinition = target.getDefinition();

        Class<?> targetReturnType = targetDefinition.getOutputType().getPhysical();
        operation.setTargetReturnType(targetReturnType.getName());

        for (DataType<?> targetFault : targetDefinition.getFaultTypes()) {
            Class<?> faultType = targetFault.getPhysical();
            operation.addTargetFaultType(faultType.getName());
        }

        List<DataType<?>> targetParams = targetDefinition.getInputTypes();
        for (DataType<?> param : targetParams) {
            Class<?> paramType = param.getPhysical();
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
        return operation.getIntents().contains(ALLOWS_BY_REFERENCE)
                || logicalAttachPoint.getIntents().contains(ALLOWS_BY_REFERENCE)
                || component.getIntents().contains(ALLOWS_BY_REFERENCE)
                || component.getDefinition().getImplementation().getIntents().contains(ALLOWS_BY_REFERENCE);
    }
}
