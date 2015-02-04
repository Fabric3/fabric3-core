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
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;
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

    public <T extends Binding> PhysicalWireDefinition generateBoundService(LogicalBinding<T> binding, URI callbackUri) {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getParent();

        ServiceContract contract = service.getServiceContract();

        List<LogicalOperation> operations = service.getOperations();

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(service);
        URI targetUri = service.getParent().getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        targetDefinition.setCallbackUri(callbackUri);

        // generate the metadata used to attach the physical wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, contract, operations);
        URI sourceUri = service.getParent().getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(contract, physicalOperations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    public <T extends Binding> PhysicalWireDefinition generateBoundServiceCallback(LogicalBinding<T> binding) {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getParent();
        ServiceContract contract = service.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = service.getCallbackOperations();

        // generate the metadata used to attach the physical callback wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service);
        URI sourceUri = component.getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target transport
        WireBindingGenerator<T> bindingGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = bindingGenerator.generateTarget(binding, callbackContract, operations);
        targetDefinition.setCallback(true);
        URI targetUri = binding.getParent().getParent().getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    public <T extends Binding> PhysicalWireDefinition generateBoundReference(LogicalBinding<T> binding) {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = reference.getOperations();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference);

        // use the binding name as the source key
        String key = binding.getDefinition().getName();
        sourceDefinition.setKey(key);

        URI sourceUri = component.getDefinition().getContributionUri();
        ClassLoader sourceLoader = classLoaderRegistry.getClassLoader(sourceUri);
        sourceDefinition.setClassLoader(sourceLoader);

        // generate the metadata used to attach the physical wire to the target transport
        WireBindingGenerator<T> targetGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(binding, contract, operations);
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            Reference<ComponentType> referenceDefinition = reference.getDefinition();
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        URI targetUri = binding.getParent().getParent().getDefinition().getContributionUri();
        ClassLoader targetLoader = classLoaderRegistry.getClassLoader(targetUri);
        targetDefinition.setClassLoader(targetLoader);

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    public <T extends Binding> PhysicalWireDefinition generateBoundReferenceCallback(LogicalBinding<T> binding) {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent<?> component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        // generate the metadata used to attach the physical callback wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, callbackContract, operations);
        URI sourceUri = binding.getParent().getParent().getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService);
        URI targetUri = callbackService.getParent().getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        targetDefinition.setCallback(true);

        Set<PhysicalOperationDefinition> operation = operationGenerator.generateOperations(operations);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);
    }

    public PhysicalWireDefinition generateWire(LogicalWire wire) {
        if (isLocal(wire)) {
            return generateLocalWire(wire);
        } else {
            return generateRemoteWire(wire);
        }
    }

    public PhysicalWireDefinition generateWireCallback(LogicalWire wire) {
        if (isLocal(wire)) {
            return generateLocalWireCallback(wire);
        } else {
            return generateRemoteWireCallback(wire);
        }
    }

    public <T extends ResourceReference> PhysicalWireDefinition generateResource(LogicalResourceReference<T> resourceReference) {
        T resourceDefinition = resourceReference.getDefinition();
        LogicalComponent<?> component = resourceReference.getParent();

        // Generates the wire source metadata
        ComponentGenerator sourceGenerator = getGenerator(component);
        @SuppressWarnings("unchecked") PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateResourceSource(resourceReference);
        URI sourceUri = component.getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // Generates the wire target metadata
        ResourceReferenceGenerator<T> targetGenerator = getGenerator(resourceDefinition);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(resourceReference);
        URI targetUri = resourceReference.getParent().getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        boolean optimizable = targetDefinition.isOptimizable();

        // Create the wire from the component to the resource
        List<LogicalOperation> sourceOperations = resourceReference.getOperations();
        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(sourceOperations);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    private boolean isLocal(LogicalWire wire) {
        String sourceZone = wire.getSource().getParent().getZone();
        String targetZone = wire.getTarget().getParent().getZone();
        return sourceZone.equals(targetZone) && (wire.getSourceBinding() == null) && (wire.getTargetBinding() == null);

    }

    /**
     * Generates a physical wire definition for a wire that is not bound to a remote transport - i.e. it is between two components hosted in the same runtime.
     *
     * @param wire the logical wire
     * @return the physical wire definition
     */
    private PhysicalWireDefinition generateLocalWire(LogicalWire wire) {
        LogicalReference reference = wire.getSource();

        LogicalService service = wire.getTarget();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getParent();
        Reference<ComponentType> referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference);
        URI sourceUri = source.getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        String key = getKey(target);
        sourceDefinition.setKey(key);

        int order = getOrder(target);
        sourceDefinition.setOrder(order);

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(target);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(service);
        URI targetUri = target.getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        List<LogicalOperation> sourceOperations = reference.getOperations();
        List<LogicalOperation> targetOperations = service.getOperations();
        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(sourceOperations, targetOperations, false);

        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(referenceContract, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    /**
     * Generates a physical wire definition for a wire that is bound to a remote transport - i.e. it is between two components hosted in different runtime
     * processes.  The source metadata is generated using a component generator for the reference parent. The target metadata is generated using the reference
     * binding. Note that metadata for the service-side binding is not generated since the service endpoint will either be provisioned previously from another
     * deployable composite or when metadata for the bound service is created by another generator.
     *
     * @param wire the logical wire
     * @return the physical wire definition
     */
    @SuppressWarnings({"unchecked"})
    private <BD extends Binding> PhysicalWireDefinition generateRemoteWire(LogicalWire wire) {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent source = reference.getParent();
        Reference<ComponentType> referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(reference.getParent());
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference);
        URI sourceUri = source.getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        String key = getKey(source);
        sourceDefinition.setKey(key);

        int order = getOrder(source);
        sourceDefinition.setOrder(order);

        LogicalBinding<BD> serviceBinding = wire.getTargetBinding();
        WireBindingGenerator<BD> targetGenerator = getGenerator(serviceBinding);

        // generate metadata to attach the physical wire to the target transport (which is the reference binding)
        List<LogicalOperation> sourceOperations = reference.getOperations();
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateServiceBindingTarget(serviceBinding, serviceContract, sourceOperations);
        URI targetUri = source.getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations;
        if (referenceContract.getClass().equals(serviceContract.getClass()) || serviceContract instanceof RemoteServiceContract) {
            physicalOperations = operationGenerator.generateOperations(sourceOperations);
        } else {
            List<LogicalOperation> targetOperations = service.getOperations();
            physicalOperations = operationGenerator.generateOperations(sourceOperations, targetOperations, true);
        }

        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    private PhysicalWireDefinition generateLocalWireCallback(LogicalWire wire) {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract referenceCallbackContract = reference.getServiceContract().getCallbackContract();
        LogicalService callbackService = targetComponent.getService(referenceCallbackContract.getInterfaceName());
        LogicalComponent sourceComponent = service.getParent();

        List<LogicalOperation> targetOperations = callbackService.getOperations();
        List<LogicalOperation> sourceOperations = service.getCallbackOperations();
        Set<PhysicalOperationDefinition> callbackOperations = operationGenerator.generateOperations(targetOperations, sourceOperations, false);

        // generate the metadata used to attach the physical callback wire to the source component (the component providing the forward service)
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service);
        URI sourceUri = sourceComponent.getDefinition().getContributionUri();
        sourceDefinition.setClassLoader(classLoaderRegistry.getClassLoader(sourceUri));

        // generate the metadata used to attach the physical callback wire to the target component (the client of the forward service)
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService);
        URI targetUri = targetComponent.getDefinition().getContributionUri();
        targetDefinition.setClassLoader(classLoaderRegistry.getClassLoader(targetUri));
        targetDefinition.setCallback(true);

        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, callbackOperations);
        pwd.setOptimizable(false);
        return pwd;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalWireDefinition generateRemoteWireCallback(LogicalWire wire) {
        LogicalReference reference = wire.getSource();
        LogicalComponent target = reference.getParent();
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract referenceCallbackContract = referenceContract.getCallbackContract();
        if (reference.getCallbackBindings().isEmpty()) {
            throw new Fabric3Exception("Callback binding not set");
        }
        LogicalBinding<?> referenceBinding = reference.getCallbackBindings().get(0);
        LogicalService callbackService = target.getService(referenceCallbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        // generate metadata to attach the physical callback wire to the source transport
        WireBindingGenerator bindingGenerator = getGenerator(referenceBinding);
        PhysicalWireSourceDefinition sourceDefinition = bindingGenerator.generateSource(referenceBinding, referenceCallbackContract, operations);
        URI targetUri = target.getDefinition().getContributionUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(targetUri);
        sourceDefinition.setClassLoader(classLoader);

        // generate the metadata used to attach the physical callback wire to the target component (the component containing the forward reference)
        ComponentGenerator componentGenerator = getGenerator(target);
        PhysicalWireTargetDefinition targetDefinition = componentGenerator.generateTarget(callbackService);
        targetDefinition.setClassLoader(classLoader);

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
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

    private boolean checkOptimization(ServiceContract serviceContract, Set<PhysicalOperationDefinition> operationDefinitions) {
        if (serviceContract.isRemotable()) {
            return false;
        }
        for (PhysicalOperationDefinition operation : operationDefinitions) {
            if (!operation.getInterceptors().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the key specified on the component definition, component type, or null
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
     * Returns the key specified on the component definition, component type, or {@link Integer#MIN_VALUE}
     *
     * @param component the component
     * @return the key or null
     */
    private int getOrder(LogicalComponent component) {
        int order = component.getDefinition().getOrder();
        if (order == Integer.MIN_VALUE) {
            Component<?> definition = component.getDefinition();
            if (definition.getComponentType() != null) {
                order = definition.getComponentType().getOrder();
            }
        }
        return order;
    }

    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return Cast.cast(generatorRegistry.getComponentGenerator(implementation.getClass()));
    }

    private <T extends ResourceReference> ResourceReferenceGenerator<T> getGenerator(T definition) {
        return Cast.cast(generatorRegistry.getResourceReferenceGenerator(definition.getClass()));
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
