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
package org.fabric3.fabric.domain.generator.wire;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.fabric.domain.generator.GeneratorNotFoundException;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.domain.generator.policy.PolicyResolver;
import org.fabric3.spi.domain.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.binding.SCABinding;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of WireGenerator.
 */
public class WireGeneratorImpl implements WireGenerator {
    private GeneratorRegistry generatorRegistry;
    private ContractMatcher matcher;
    private PolicyResolver policyResolver;
    private PhysicalOperationGenerator operationGenerator;

    public WireGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                             @Reference ContractMatcher matcher,
                             @Reference PolicyResolver policyResolver,
                             @Reference PhysicalOperationGenerator operationGenerator) {
        this.generatorRegistry = generatorRegistry;
        this.matcher = matcher;
        this.policyResolver = policyResolver;
        this.operationGenerator = operationGenerator;
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundService(LogicalBinding<T> binding, URI callbackUri) throws GenerationException {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getLeafComponent();

        // Use the leaf service contract to bind to the transport in case of service promotions.
        // The overriding service contract (i.e. the one on the promoted service) is used for wire matching but not for binding to the transport
        // since doing so would require matching to the original contract and potential parameter data transformation. For example, a promoted
        // service contract may be expressed in WSDL and the target service contract in Java.
        ServiceContract contract = service.getLeafService().getServiceContract();

        List<LogicalOperation> operations = service.getOperations();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolvePolicies(binding);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallbackUri(callbackUri);

        // generate the metadata used to attach the physical wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, contract, operations, sourcePolicy);
        sourceDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(contract, physicalOperations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundServiceCallback(LogicalBinding<T> binding) throws GenerationException {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getLeafComponent();
        ServiceContract contract = service.getLeafService().getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = service.getCallbackOperations();

        // resolve callback policies
        PolicyResult policyResult = policyResolver.resolveCallbackPolicies(binding);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical callback wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        // generate the metadata used to attach the physical callback wire to the target transport
        WireBindingGenerator<T> bindingGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = bindingGenerator.generateTarget(binding, callbackContract, operations, targetPolicy);
        targetDefinition.setCallback(true);
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundReference(LogicalBinding<T> binding) throws GenerationException {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        List<LogicalOperation> operations = reference.getOperations();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolvePolicies(binding);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);

        // use the binding name as the source key
        String key = binding.getDefinition().getName();
        sourceDefinition.setKey(key);

        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        // generate the metadata used to attach the physical wire to the target transport
        WireBindingGenerator<T> targetGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(binding, contract, operations, targetPolicy);
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            AbstractReference referenceDefinition = reference.getDefinition();
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundReferenceCallback(LogicalBinding<T> binding) throws GenerationException {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent<?> component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolveCallbackPolicies(binding);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical callback wire to the source transport
        WireBindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, callbackContract, operations, targetPolicy);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        // generate the metadata used to attach the physical callback wire to the target component
        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService, sourcePolicy);
        targetDefinition.setClassLoaderId(callbackService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallback(true);

        Set<PhysicalOperationDefinition> operation = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);
    }

    public PhysicalWireDefinition generateWire(LogicalWire wire) throws GenerationException {
        if (isLocal(wire)) {
            return generateLocalWire(wire);
        } else {
            return generateRemoteWire(wire);
        }
    }

    public PhysicalWireDefinition generateWireCallback(LogicalWire wire) throws GenerationException {
        if (isLocal(wire)) {
            return generateLocalWireCallback(wire);
        } else {
            return generateRemoteWireCallback(wire);
        }
    }

    public <T extends ResourceReferenceDefinition> PhysicalWireDefinition generateResource(LogicalResourceReference<T> resourceReference)
            throws GenerationException {
        T resourceDefinition = resourceReference.getDefinition();
        LogicalComponent<?> component = resourceReference.getParent();

        // Generates the wire source metadata
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateResourceSource(resourceReference);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        // Generates the wire target metadata
        ResourceReferenceGenerator<T> targetGenerator = getGenerator(resourceDefinition);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(resourceReference);
        targetDefinition.setClassLoaderId(resourceReference.getParent().getDefinition().getContributionUri());
        boolean optimizable = targetDefinition.isOptimizable();

        // Create the wire from the component to the resource
        List<LogicalOperation> sourceOperations = resourceReference.getOperations();
        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(sourceOperations, false, null);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    private boolean isLocal(LogicalWire wire) {
        String sourceZone = wire.getSource().getParent().getZone();
        String targetZone = wire.getTarget().getParent().getZone();
        return sourceZone.equals(targetZone) && (wire.getSourceBinding() == null || wire.getSourceBinding().getDefinition() instanceof SCABinding) && (
                wire.getTargetBinding() == null || wire.getTargetBinding().getDefinition() instanceof SCABinding);

    }

    /**
     * Generates a physical wire definition for a wire that is not bound to a remote transport - i.e. it is between two components hosted in the same runtime.
     *
     * @param wire the logical wire
     * @return the physical wire definition
     * @throws GenerationException if an error occurs during generation
     */
    private PhysicalWireDefinition generateLocalWire(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();

        // use the leaf service to optimize data paths - e.g. a promoted service may use a different service contract and databinding than the leaf
        LogicalService service = wire.getTarget().getLeafService();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getLeafComponent();
        AbstractReference referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolveLocalPolicies(wire);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());

        String key = getKey(target);
        sourceDefinition.setKey(key);

        int order = getOrder(target);
        sourceDefinition.setOrder(order);

        // generate the metadata used to attach the physical wire to the target component
        ComponentGenerator targetGenerator = getGenerator(target);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        Set<PhysicalOperationDefinition> operations;
        if (referenceContract.getClass().equals(serviceContract.getClass())) {
            List<LogicalOperation> sourceOperations = reference.getOperations();
            operations = operationGenerator.generateOperations(sourceOperations, false, policyResult);
        } else {
            List<LogicalOperation> sourceOperations = reference.getOperations();
            List<LogicalOperation> targetOperations = service.getOperations();
            operations = operationGenerator.generateOperations(sourceOperations, targetOperations, false, policyResult);
        }

        QName sourceDeployable = source.getDeployable();
        QName targetDeployable = target.getDeployable();

        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, operations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(referenceContract, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    /**
     * Generates a physical wire definition for a wire that is bound to a remote transport - i.e. it is between two components hosted in different runtime
     * processes.
     * <p/>
     * The source metadata is generated using a component generator for the reference parent. The target metadata is generated using the reference binding. Note
     * that metadata for the service-side binding is not generated since the service endpoint will either be provisioned previously from another deployable
     * composite or when metadata for the bound service is created by another generator.
     *
     * @param wire the logical wire
     * @return the physical wire definition
     * @throws GenerationException if an error occurs during generation
     */
    @SuppressWarnings({"unchecked"})
    private <BD extends BindingDefinition> PhysicalWireDefinition generateRemoteWire(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getLeafComponent();
        AbstractReference referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();

        PolicyResult policyResult = policyResolver.resolveRemotePolicies(wire);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate the metadata used to attach the physical wire to the source component
        ComponentGenerator sourceGenerator = getGenerator(reference.getParent());
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());

        String key = getKey(source);
        sourceDefinition.setKey(key);

        int order = getOrder(source);
        sourceDefinition.setOrder(order);

        LogicalBinding<BD> serviceBinding = wire.getTargetBinding();
        WireBindingGenerator<BD> targetGenerator = getGenerator(serviceBinding);

        // generate metadata to attach the physical wire to the target transport (which is the reference binding)
        List<LogicalOperation> sourceOperations = reference.getOperations();
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateServiceBindingTarget(serviceBinding,
                                                                                                     serviceContract,
                                                                                                     sourceOperations,
                                                                                                     targetPolicy);
        targetDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations;
        if (referenceContract.getClass().equals(serviceContract.getClass()) || serviceContract instanceof RemoteServiceContract) {
            physicalOperations = operationGenerator.generateOperations(sourceOperations, true, policyResult);
        } else {
            List<LogicalOperation> targetOperations = service.getOperations();
            physicalOperations = operationGenerator.generateOperations(sourceOperations, targetOperations, true, policyResult);
        }

        QName sourceDeployable = source.getDeployable();
        QName targetDeployable = target.getDeployable();

        return new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, physicalOperations);
    }

    private PhysicalWireDefinition generateLocalWireCallback(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract referenceCallbackContract = reference.getServiceContract().getCallbackContract();
        LogicalService callbackService = targetComponent.getService(referenceCallbackContract.getInterfaceName());
        LogicalComponent sourceComponent = service.getLeafComponent();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolveLocalCallbackPolicies(wire);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        Set<PhysicalOperationDefinition> callbackOperations;
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (referenceCallbackContract.getClass().equals(callbackContract.getClass())) {
            List<LogicalOperation> operations = service.getCallbackOperations();
            callbackOperations = operationGenerator.generateOperations(operations, false, policyResult);
        } else {
            List<LogicalOperation> targetOperations = callbackService.getOperations();
            List<LogicalOperation> sourceOperations = service.getCallbackOperations();
            callbackOperations = operationGenerator.generateOperations(targetOperations, sourceOperations, false, policyResult);
        }

        // generate the metadata used to attach the physical callback wire to the source component (the component providing the forward service)
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(sourceComponent.getDefinition().getContributionUri());

        // generate the metadata used to attach the physical callback wire to the target component (the client of the forward service)
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService, targetPolicy);
        targetDefinition.setClassLoaderId(targetComponent.getDefinition().getContributionUri());
        targetDefinition.setCallback(true);

        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, callbackOperations);
        pwd.setOptimizable(false);
        return pwd;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalWireDefinition generateRemoteWireCallback(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalComponent target = reference.getParent();
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract referenceCallbackContract = referenceContract.getCallbackContract();
        if (reference.getCallbackBindings().isEmpty()) {
            throw new GenerationException("Callback binding not set");
        }
        LogicalBinding<?> referenceBinding = reference.getCallbackBindings().get(0);
        LogicalService callbackService = target.getService(referenceCallbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        // resolve policies
        PolicyResult policyResult = policyResolver.resolveRemoteCallbackPolicies(wire);
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        // generate metadata to attach the physical callback wire to the source transport
        WireBindingGenerator bindingGenerator = getGenerator(referenceBinding);
        PhysicalWireSourceDefinition sourceDefinition = bindingGenerator.generateSource(referenceBinding, referenceCallbackContract, operations, targetPolicy);
        URI contributionUri = target.getDefinition().getContributionUri();
        sourceDefinition.setClassLoaderId(contributionUri);

        // generate the metadata used to attach the physical callback wire to the target component (the component containing the forward reference)
        ComponentGenerator componentGenerator = getGenerator(target);
        PhysicalWireTargetDefinition targetDefinition = componentGenerator.generateTarget(callbackService, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());

        // generate the metadata for interceptors that are attached to wire invocation chains, e.g. policy implemented by an interceptor
        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    private <S extends LogicalComponent<?>> URI generateCallbackUri(S source, ServiceContract contract, String referenceName) throws GenerationException {
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
            throw new CallbackServiceNotFoundException(
                    "Callback service not found: " + name + " on component: " + uri + " originating from reference :" + referenceName);
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
            Implementation implementation = component.getDefinition().getImplementation();
            if (implementation != null && implementation.getComponentType() != null) {
                key = implementation.getComponentType().getKey();
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
            ComponentDefinition<?> definition = component.getDefinition();
            Implementation<?> implementation = definition.getImplementation();
            if (implementation != null && implementation.getComponentType() != null) {
                order = implementation.getComponentType().getOrder();
            }
        }
        return order;
    }

    @SuppressWarnings("unchecked")
    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) throws GeneratorNotFoundException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return (ComponentGenerator<C>) generatorRegistry.getComponentGenerator(implementation.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends ResourceReferenceDefinition> ResourceReferenceGenerator<T> getGenerator(T definition) throws GeneratorNotFoundException {
        return (ResourceReferenceGenerator<T>) generatorRegistry.getResourceReferenceGenerator(definition.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> WireBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (WireBindingGenerator<T>) generatorRegistry.getBindingGenerator(binding.getDefinition().getClass());
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
