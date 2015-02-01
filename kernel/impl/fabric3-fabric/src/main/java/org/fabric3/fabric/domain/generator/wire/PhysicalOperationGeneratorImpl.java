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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class PhysicalOperationGeneratorImpl implements PhysicalOperationGenerator {
    private OperationResolver operationResolver;
    private GeneratorRegistry generatorRegistry;

    public PhysicalOperationGeneratorImpl(@Reference OperationResolver operationResolver, @Reference GeneratorRegistry generatorRegistry) {
        this.operationResolver = operationResolver;
        this.generatorRegistry = generatorRegistry;
    }

    public Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> operations) throws Fabric3Exception {

        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<>(operations.size());

        for (LogicalOperation operation : operations) {
            PhysicalOperationDefinition physicalOperation = generate(operation);
            physicalOperations.add(physicalOperation);
        }
        return physicalOperations;
    }

    public Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> sources, List<LogicalOperation> targets, boolean remote)
            throws Fabric3Exception {
        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<>(sources.size());
        for (LogicalOperation source : sources) {
            LogicalOperation target = operationResolver.resolve(source, targets);
            PhysicalOperationDefinition physicalOperation = generate(source, target);
            physicalOperations.add(physicalOperation);
            if (!remote) {
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptors(source, target);
                physicalOperation.setInterceptors(interceptors);
            }
        }
        return physicalOperations;
    }

    /**
     * Generates interceptor definitions for the operation based on a set of resolved policies.
     *
     * @param source the operation
     * @param target the target operation
     * @return the interceptor definitions
     * @throws Fabric3Exception if a generation error occurs
     */
    private Set<PhysicalInterceptorDefinition> generateInterceptors(LogicalOperation source, LogicalOperation target) throws Fabric3Exception {
        Set<PhysicalInterceptorDefinition> interceptors = new LinkedHashSet<>();
        for (InterceptorGenerator interceptorGenerator : generatorRegistry.getInterceptorGenerators()) {
            Optional<PhysicalInterceptorDefinition> optional = interceptorGenerator.generate(source, target);
            optional.ifPresent(interceptors::add);
        }
        return interceptors;
    }

    /**
     * Generates a PhysicalOperationDefinition when the source reference and target service contracts are the same.
     *
     * @param source the logical operation to generate from
     * @return the PhysicalOperationDefinition
     */
    private PhysicalOperationDefinition generate(LogicalOperation source) {
        Operation o = source.getDefinition();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        operation.setOneWay(o.isOneWay());
        operation.setRemotable(o.isRemotable());

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
     * @return the PhysicalOperationDefinition
     */
    private PhysicalOperationDefinition generate(LogicalOperation source, LogicalOperation target) {
        Operation o = source.getDefinition();
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());

        operation.setRemotable(o.isRemotable());
        operation.setOneWay(o.isOneWay());
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

}
