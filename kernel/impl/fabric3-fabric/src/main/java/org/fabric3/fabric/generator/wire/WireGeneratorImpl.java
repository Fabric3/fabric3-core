/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.binding.SCABinding;
import org.fabric3.spi.policy.EffectivePolicy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * Default implementation of WireGenerator.
 *
 * @version $Rev$ $Date$
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

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundService(LogicalBinding<T> binding, URI callbackUri)
            throws GenerationException {
        checkService(binding);
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getLeafComponent();

        // Use the leaf service contract to bind to the transport in case of service promotions.
        // The overriding service contract (i.e. the one on the promoted service) is used for wire matching but not for binding to the transport
        // since doing so would require matching to the original contract and potential parameter data transformation. For example, a promoted
        // service contract may be expressed in WSDL and the target service contract in Java.
        ServiceContract contract = service.getLeafService().getServiceContract();

        LogicalBinding<RemoteBindingDefinition> targetBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, service);

        List<LogicalOperation> operations = service.getOperations();
        PolicyResult policyResult = resolvePolicies(operations, binding, targetBinding, null, component);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallbackUri(callbackUri);

        BindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, contract, operations, sourcePolicy);
        sourceDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());

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

        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, service);
        List<LogicalOperation> operations = service.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(operations, sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        BindingGenerator<T> bindingGenerator = getGenerator(binding);
        PhysicalTargetDefinition targetDefinition =
                bindingGenerator.generateTarget(binding, callbackContract, operations, targetPolicy);
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundReference(LogicalBinding<T> binding) throws GenerationException {
        checkReference(binding);
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();
        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, reference);

        List<LogicalOperation> operations = reference.getOperations();
        PolicyResult policyResult = resolvePolicies(operations, sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator<T> targetGenerator = getGenerator(binding);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(binding, contract, operations, targetPolicy);
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            ReferenceDefinition referenceDefinition = reference.getDefinition();
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

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

        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, reference);

        List<LogicalOperation> operations = reference.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(operations, sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator<T> sourceGenerator = getGenerator(binding);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, callbackContract, operations, targetPolicy);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService, sourcePolicy);
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

    public <T extends ResourceDefinition> PhysicalWireDefinition generateResource(LogicalResource<T> resource) throws GenerationException {
        T resourceDefinition = resource.getResourceDefinition();
        LogicalComponent<?> component = resource.getParent();

        // Generates the wire source metadata
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateResourceSource(resource);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        // Generates the wire target metadata
        ResourceGenerator<T> targetGenerator = getGenerator(resourceDefinition);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateWireTarget(resource);
        targetDefinition.setClassLoaderId(resource.getParent().getDefinition().getContributionUri());
        boolean optimizable = targetDefinition.isOptimizable();

        // Create the wire from the component to the resource
        List<LogicalOperation> sourceOperations = resource.getOperations();
        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(sourceOperations, false, null);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    private boolean isLocal(LogicalWire wire) {
        // at this point an SCA binding can only exist for local wires since SCA bindings for remote wires will have been replaced
        // with concrete bindings
        return (wire.getSourceBinding() == null ||wire.getSourceBinding().getDefinition() instanceof SCABinding)
                && (wire.getTargetBinding() == null ||wire.getTargetBinding().getDefinition() instanceof SCABinding);
    }
    
    /**
     * Generates a physical wire definition for a wire that is not bound to a remote transport - i.e. it is between two components hosted in the same
     * runtime.
     *
     * @param wire the logical wire
     * @return the physical wire definiton
     * @throws GenerationException if an error occurs during generation
     */
    private PhysicalWireDefinition generateLocalWire(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();

        // use the leaf service to optimize data paths - e.g. a promoted service may use a different service contract and databinding than the leaf
        LogicalService service = wire.getTarget().getLeafService();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getLeafComponent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(reference.getOperations(), sourceBinding, targetBinding, source, target);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(target);
        // generate metadata for the target side of the wire
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        String key = target.getDefinition().getKey();
        sourceDefinition.setKey(key);

        Set<PhysicalOperationDefinition> operations;
        if (referenceContract.getClass().equals(serviceContract.getClass())) {
            List<LogicalOperation> sourceOperations = reference.getOperations();
            operations = operationGenerator.generateOperations(sourceOperations, false, policyResult);
        } else {
            List<LogicalOperation> sourceOperations = reference.getOperations();
            List<LogicalOperation> targetOperations = service.getOperations();
            operations = operationGenerator.generateOperations(sourceOperations, targetOperations, false, policyResult);
        }

        QName sourceDeployable = null;
        QName targetDeployable = null;
        if (LogicalState.NEW == target.getState()) {
            sourceDeployable = source.getDeployable();
            targetDeployable = target.getDeployable();
        }

        PhysicalWireDefinition pwd =
                new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, operations);
        boolean optimizable =
                sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(referenceContract, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    /**
     * Generates a physical wire definition for a wire that is bound to a remote transport - i.e. it is between two components hosted in different
     * runtime processes.
     * <p/>
     * The source metadata is generated using a component generator for the reference parent. The target metadata is generated using the reference
     * binding. Note that metadata for the service-side binding is not generated since the service endpoint will either be provisioned previously from
     * another deployable composite or when metadata for the bound service is created by another generator.
     *
     * @param wire the logical wire
     * @return the physical wire definiton
     * @throws GenerationException if an error occurs during generation
     */
    @SuppressWarnings({"unchecked"})
    private <BD extends BindingDefinition> PhysicalWireDefinition generateRemoteWire(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getLeafComponent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();

        LogicalBinding<BD> serviceBinding = wire.getTargetBinding();

        List<LogicalOperation> sourceOperations = reference.getOperations();
        PolicyResult policyResult = resolvePolicies(sourceOperations, serviceBinding, serviceBinding, source, target);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator sourceGenerator = getGenerator(reference.getParent());

        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        String key = source.getDefinition().getKey();
        sourceDefinition.setKey(key);

        BindingGenerator<BD> targetGenerator = getGenerator(serviceBinding);

        // generate metadata for the target side of the wire which is the reference binding
        PhysicalTargetDefinition targetDefinition =
                targetGenerator.generateServiceBindingTarget(serviceBinding, serviceContract, sourceOperations, targetPolicy);
        targetDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        Set<PhysicalOperationDefinition> physicalOperations;
        if (referenceContract.getClass().equals(serviceContract.getClass())) {
            physicalOperations = operationGenerator.generateOperations(sourceOperations, true, policyResult);
        } else {
            List<LogicalOperation> targetOperations = service.getOperations();
            physicalOperations = operationGenerator.generateOperations(sourceOperations, targetOperations, true, policyResult);
        }

        QName sourceDeployable = null;
        QName targetDeployable = null;
        if (LogicalState.NEW == target.getState()) {
            sourceDeployable = source.getDeployable();
            targetDeployable = target.getDeployable();
        }

        return new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, physicalOperations);
    }
    
    private PhysicalWireDefinition generateLocalWireCallback(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract referenceCallbackContract = reference.getServiceContract().getCallbackContract();
        LogicalService callbackService = targetComponent.getService(referenceCallbackContract.getInterfaceName());

        LogicalBinding<LocalBindingDefinition> sourceBinding =
                new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, callbackService);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalComponent sourceComponent = service.getLeafComponent();
        PolicyResult policyResult = resolvePolicies(service.getCallbackOperations(), sourceBinding, targetBinding, sourceComponent, targetComponent);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        Set<PhysicalOperationDefinition> callbackOperations;
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (referenceCallbackContract.getClass().equals(callbackContract.getClass())) {
            List<LogicalOperation> operations = callbackService.getOperations();
            callbackOperations = operationGenerator.generateOperations(operations, false, policyResult);
        } else {
            List<LogicalOperation> targetOperations = callbackService.getOperations();
            List<LogicalOperation> sourceOperations = service.getCallbackOperations();
            callbackOperations = operationGenerator.generateOperations(targetOperations, sourceOperations, false, policyResult);
        }

        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(sourceComponent.getDefinition().getContributionUri());

        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackService, targetPolicy);
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
        LogicalBinding<?> referenceBinding = reference.getCallbackBindings().get(0);
        LogicalService callbackService = target.getService(referenceCallbackContract.getInterfaceName());
        List<LogicalOperation> operations = reference.getCallbackOperations();

        LogicalBinding<LocalBindingDefinition> sourceBinding =
                new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, callbackService);
        PolicyResult policyResult = resolvePolicies(operations, sourceBinding, referenceBinding, target, null);

        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator componentGenerator = getGenerator(target);

        PhysicalTargetDefinition targetDefinition = componentGenerator.generateTarget(callbackService, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());

        BindingGenerator bindingGenerator = getGenerator(referenceBinding);

        // generate metadata for the target side of the wire which is the reference binding
        PhysicalSourceDefinition sourceDefinition =
                bindingGenerator.generateSource(referenceBinding, referenceCallbackContract, operations, targetPolicy);
        URI contributionUri = target.getDefinition().getContributionUri();
        sourceDefinition.setClassLoaderId(contributionUri);

        Set<PhysicalOperationDefinition> physicalOperations = operationGenerator.generateOperations(operations, true, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, physicalOperations);
    }

    private PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                         LogicalBinding<?> sourceBinding,
                                         LogicalBinding<?> targetBinding,
                                         LogicalComponent<?> source,
                                         LogicalComponent<?> target) throws PolicyGenerationException {
        try {
            return policyResolver.resolvePolicies(operations, sourceBinding, targetBinding, source, target);
        } catch (PolicyResolutionException e) {
            throw new PolicyGenerationException(e);
        }
    }

    private <S extends LogicalComponent<?>> URI generateCallbackUri(S source, ServiceContract contract, String referenceName)
            throws GenerationException {
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
            throw new CallbackServiceNotFoundException("Callback service not found: "
                    + name + " on component: " + uri + " originating from reference :" + referenceName, name);
        }
        return URI.create(source.getUri().toString() + "#" + candidate.getDefinition().getName());
    }

    private boolean checkOptimization(ServiceContract serviceContract, Set<PhysicalOperationDefinition> operationDefinitions) {
        if (serviceContract.isConversational()) {
            return false;
        }
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

    @SuppressWarnings("unchecked")
    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) throws GeneratorNotFoundException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return (ComponentGenerator<C>) generatorRegistry.getComponentGenerator(implementation.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends ResourceDefinition> ResourceGenerator<T> getGenerator(T definition) throws GeneratorNotFoundException {
        return (ResourceGenerator<T>) generatorRegistry.getResourceWireGenerator(definition.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> BindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (BindingGenerator<T>) generatorRegistry.getBindingGenerator(binding.getDefinition().getClass());
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
