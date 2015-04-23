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

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.spi.domain.generator.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWire;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.util.Cast;

/**
 * Default implementation of WireGenerator.
 */
public class WireGeneratorImpl implements WireGenerator {
    private GeneratorRegistry generatorRegistry;
    private ContractMatcher matcher;
    private PhysicalOperationGenerator operationGenerator;
    private ClassLoaderRegistry classLoaderRegistry;

    public WireGeneratorImpl(@org.oasisopen.sca.annotation.Reference GeneratorRegistry generatorRegistry,
                             @org.oasisopen.sca.annotation.Reference ContractMatcher matcher,
                             @org.oasisopen.sca.annotation.Reference PhysicalOperationGenerator operationGenerator,
                             @org.oasisopen.sca.annotation.Reference ClassLoaderRegistry classLoaderRegistry) {
        this.generatorRegistry = generatorRegistry;
        this.matcher = matcher;
        this.operationGenerator = operationGenerator;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public <T extends Binding> PhysicalWire generateService(LogicalBinding<T> binding, URI callbackUri) {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getParent();

        ServiceContract contract = service.getServiceContract();

        List<LogicalOperation> operations = service.getOperations();

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTarget target = targetGenerator.generateTarget(service);
        URI targetUri = service.getParent().getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        target.setCallbackUri(callbackUri);

        // generate the metadata used to attach the physical wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSource source = sourceGenerator.generateSource(binding, contract, operations);
        URI sourceUri = service.getParent().getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperation> physicalOperations = operationGenerator.generateOperations(operations);
        PhysicalWire physicalWire = new PhysicalWire(source, target, physicalOperations);
        boolean optimizable = source.isOptimizable() && target.isOptimizable() && checkOptimization(contract, physicalOperations);
        physicalWire.setOptimizable(optimizable);
        return physicalWire;
    }

    public <T extends Binding> PhysicalWire generateServiceCallback(LogicalBinding<T> binding) {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getParent();
        ServiceContract contract = service.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = service.getCallbackOperations();

        // generate the metadata used to attach the physical callback wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSource source = sourceGenerator.generateCallbackSource(service);
        URI sourceUri = component.getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target transport
        WireBindingGenerator<T> bindingGenerator = getGenerator(binding);
        PhysicalWireTarget target = bindingGenerator.generateTarget(binding, callbackContract, operations);
        target.setCallback(true);
        URI targetUri = binding.getParent().getParent().getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperation> physicalOperations = operationGenerator.generateOperations(operations);
        return new PhysicalWire(source, target, physicalOperations);
    }

    public <T extends Binding> PhysicalWire generateReference(LogicalBinding<T> binding) {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = reference.getOperations();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSource source = sourceGenerator.generateSource(reference);

        // use the binding name as the source key
        String key = binding.getDefinition().getName();
        source.setKey(key);

        URI sourceUri = component.getDefinition().getContributionUri();
        ClassLoader sourceLoader = classLoaderRegistry.getClassLoader(sourceUri);
        source.setClassLoader(sourceLoader);

        // generate the metadata used to attach the physical wire to the target transport
        WireBindingGenerator<T> targetGenerator = getGenerator(binding);
        PhysicalWireTarget target = targetGenerator.generateTarget(binding, contract, operations);
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            Reference<ComponentType> referenceDefinition = reference.getDefinition();
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            target.setCallbackUri(callbackUri);
        }
        URI targetUri = binding.getParent().getParent().getDefinition().getContributionUri();
        ClassLoader targetLoader = classLoaderRegistry.getClassLoader(targetUri);
        target.setClassLoader(targetLoader);

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperation> physicalOperations = operationGenerator.generateOperations(operations);
        return new PhysicalWire(source, target, physicalOperations);
    }

    public <T extends Binding> PhysicalWire generateReferenceCallback(LogicalBinding<T> binding) {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent<?> component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        // generate the metadata used to attach the physical callback wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSource source = sourceGenerator.generateSource(binding, callbackContract, operations);
        URI sourceUri = binding.getParent().getParent().getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTarget target = targetGenerator.generateTarget(callbackService);
        URI targetUri = callbackService.getParent().getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        target.setCallback(true);

        Set<PhysicalOperation> operation = operationGenerator.generateOperations(operations);
        return new PhysicalWire(source, target, operation);
    }

    public PhysicalWire generateWire(LogicalWire wire) {
        return generateLocalWire(wire);
    }

    public PhysicalWire generateWireCallback(LogicalWire wire) {
        return generateLocalWireCallback(wire);
    }

    public <T extends ResourceReference> PhysicalWire generateResource(LogicalResourceReference<T> logicalReference) {
        T reference = logicalReference.getDefinition();
        LogicalComponent<?> component = logicalReference.getParent();

        // Generates the wire source metadata
        ComponentGenerator sourceGenerator = getGenerator(component);
        @SuppressWarnings("unchecked") PhysicalWireSource source = sourceGenerator.generateResourceSource(logicalReference);
        URI sourceUri = component.getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // Generates the wire target metadata
        ResourceReferenceGenerator<T> targetGenerator = getGenerator(reference);
        PhysicalWireTarget target = targetGenerator.generateWireTarget(logicalReference);
        URI targetUri = logicalReference.getParent().getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        boolean optimizable = target.isOptimizable();

        // Create the wire from the component to the resource
        List<LogicalOperation> sourceOperations = logicalReference.getOperations();
        Set<PhysicalOperation> operations = operationGenerator.generateOperations(sourceOperations);
        PhysicalWire physicalWire = new PhysicalWire(source, target, operations);
        physicalWire.setOptimizable(optimizable);
        return physicalWire;
    }

    /**
     * Generates a physical wire for a wire that is not bound to a remote transport - i.e. it is between two components hosted in the same runtime.
     *
     * @param wire the logical wire
     * @return the physical wire definition
     */
    private PhysicalWire generateLocalWire(LogicalWire wire) {
        LogicalReference reference = wire.getSource();

        LogicalService service = wire.getTarget();
        LogicalComponent sourceComponent = reference.getParent();
        LogicalComponent targetComponent = service.getParent();
        Reference<ComponentType> referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        PhysicalWireSource source = sourceGenerator.generateSource(reference);
        URI sourceUri = sourceComponent.getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        String key = getKey(targetComponent);
        source.setKey(key);

        int order = getOrder(targetComponent);
        source.setOrder(order);

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PhysicalWireTarget target = targetGenerator.generateTarget(service);
        URI targetUri = targetComponent.getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(sourceComponent, callbackContract, referenceDefinition.getName());
            target.setCallbackUri(callbackUri);
        }

        List<LogicalOperation> sourceOperations = reference.getOperations();
        List<LogicalOperation> targetOperations = service.getOperations();
        Set<PhysicalOperation> operations = operationGenerator.generateOperations(sourceOperations, targetOperations, false);

        PhysicalWire physicalWire = new PhysicalWire(source, target, operations);
        boolean optimizable = source.isOptimizable() && target.isOptimizable() && checkOptimization(referenceContract, operations);
        physicalWire.setOptimizable(optimizable);
        return physicalWire;
    }

    private PhysicalWire generateLocalWireCallback(LogicalWire wire) {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract referenceCallbackContract = reference.getServiceContract().getCallbackContract();
        LogicalService callbackService = targetComponent.getService(referenceCallbackContract.getInterfaceName());
        LogicalComponent sourceComponent = service.getParent();

        List<LogicalOperation> targetOperations = callbackService.getOperations();
        List<LogicalOperation> sourceOperations = service.getCallbackOperations();
        Set<PhysicalOperation> callbackOperations = operationGenerator.generateOperations(targetOperations, sourceOperations, false);

        // generate the metadata used to attach the physical callback wire to the source component (the component providing the forward service)
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        PhysicalWireSource source = sourceGenerator.generateCallbackSource(service);
        URI sourceUri = sourceComponent.getDefinition().getContributionUri();
        source.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target component (the client of the forward service)
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PhysicalWireTarget target = targetGenerator.generateTarget(callbackService);
        URI targetUri = targetComponent.getDefinition().getContributionUri();
        target.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        target.setCallback(true);

        PhysicalWire physicalWire = new PhysicalWire(source, target, callbackOperations);
        physicalWire.setOptimizable(false);
        return physicalWire;
    }

    private <S extends LogicalComponent<?>> URI generateCallbackUri(S source, ServiceContract contract, String referenceName) {
        LogicalService candidate = null;
        for (LogicalService entry : source.getServices()) {
            MatchResult result = matcher.isAssignableFrom(contract, entry.getServiceContract(), false);
            if (result.isAssignable()) {
                candidate = entry;
                break;
            }
        }
        if (candidate == null) {
            String name = contract.getInterfaceName();
            URI uri = source.getUri();
            throw new Fabric3Exception("Callback service not found: " + name + " on component: " + uri + " originating from reference :" + referenceName);
        }
        return URI.create(source.getUri().toString() + "#" + candidate.getDefinition().getName());
    }

    private boolean checkOptimization(ServiceContract serviceContract, Set<PhysicalOperation> operationDefinitions) {
        if (serviceContract.isRemotable()) {
            return false;
        }
        for (PhysicalOperation operation : operationDefinitions) {
            if (!operation.getInterceptors().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the key specified on the component, component type, or null
     *
     * @param component the component
     * @return the key or null
     */
    private String getKey(LogicalComponent component) {
        String key = component.getDefinition().getKey();
        if (key == null) {
            // check if the key was specified in the component type
            if (component.getDefinition().getComponentType() != null) {
                key = component.getDefinition().getComponentType().getKey();
            }
        }
        return key;
    }

    /**
     * Returns the key specified on the component, component type, or {@link Integer#MIN_VALUE}
     *
     * @param logicalComponent the component
     * @return the key or null
     */
    private int getOrder(LogicalComponent logicalComponent) {
        int order = logicalComponent.getDefinition().getOrder();
        if (order == Integer.MIN_VALUE) {
            Component<?> component = logicalComponent.getDefinition();
            if (component.getComponentType() != null) {
                order = component.getComponentType().getOrder();
            }
        }
        return order;
    }

    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return Cast.cast(generatorRegistry.getComponentGenerator(implementation.getClass()));
    }

    private <T extends ResourceReference> ResourceReferenceGenerator<T> getGenerator(T reference) {
        return Cast.cast(generatorRegistry.getResourceReferenceGenerator(reference.getClass()));
    }

    private <T extends Binding> WireBindingGenerator<T> getGenerator(LogicalBinding<T> binding) {
        return Cast.cast(generatorRegistry.getBindingGenerator(binding.getDefinition().getClass()));
    }

    private void checkService(LogicalBinding<?> binding) {
        if (!(binding.getParent() instanceof LogicalService)) {
            throw new AssertionError("Expected " + LogicalService.class.getName() + " as parent to binding");
        }
    }

    private void checkReference(LogicalBinding binding) {
        if (!(binding.getParent() instanceof LogicalReference)) {
            throw new AssertionError("Expected " + LogicalReference.class.getName() + " as parent to binding");
        }
    }

}
