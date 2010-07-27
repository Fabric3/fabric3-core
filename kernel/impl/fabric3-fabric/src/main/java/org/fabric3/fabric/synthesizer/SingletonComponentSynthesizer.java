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
package org.fabric3.fabric.synthesizer;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.implementation.system.singleton.SingletonComponent;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.cm.RegistrationException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.synthesize.ComponentRegistrationException;
import org.fabric3.spi.synthesize.ComponentSynthesizer;
import org.fabric3.spi.synthesize.InvalidServiceContractException;

import static org.fabric3.host.Names.BOOT_CONTRIBUTION;

/**
 * Implementation that synthesizes a singleton component from an existing object instance.
 *
 * @version $Rev$ $Date$
 */
public class SingletonComponentSynthesizer implements ComponentSynthesizer {

    private ImplementationProcessor<SystemImplementation> implementationProcessor;
    private AtomicComponentInstantiator instantiator;
    private LogicalComponentManager lcm;
    private ComponentManager componentManager;
    private JavaContractProcessor contractProcessor;
    private ScopeContainer scopeContainer;

    @Constructor
    public SingletonComponentSynthesizer(@Reference ImplementationProcessor<SystemImplementation> implementationProcessor,
                                         @Reference AtomicComponentInstantiator instantiator,
                                         @Reference LogicalComponentManager lcm,
                                         @Reference ComponentManager componentManager,
                                         @Reference JavaContractProcessor contractProcessor,
                                         @Reference ScopeRegistry registry) {
        this(implementationProcessor, instantiator, lcm, componentManager, contractProcessor, registry.getScopeContainer(Scope.COMPOSITE));
    }

    public SingletonComponentSynthesizer(ImplementationProcessor<SystemImplementation> implementationProcessor,
                                         AtomicComponentInstantiator instantiator,
                                         LogicalComponentManager lcm,
                                         ComponentManager componentManager,
                                         JavaContractProcessor contractProcessor,
                                         ScopeContainer scopeContainer) {
        this.implementationProcessor = implementationProcessor;
        this.instantiator = instantiator;
        this.lcm = lcm;
        this.componentManager = componentManager;
        this.contractProcessor = contractProcessor;
        this.scopeContainer = scopeContainer;
    }

    public <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws ComponentRegistrationException {
        try {
            LogicalComponent<?> logical = createLogicalComponent(name, type, instance, introspect);
            AtomicComponent physical = createPhysicalComponent(logical, instance);
            componentManager.register(physical);
            scopeContainer.register(physical);
            // initialize the component - needed for reinjection to work
            WorkContext workContext = new WorkContext();
            scopeContainer.getWrapper(physical, workContext);
        } catch (RegistrationException e) {
            throw new ComponentRegistrationException(e);
        } catch (AssemblyException e) {
            throw new ComponentRegistrationException(e);
        } catch (InstanceLifecycleException e) {
            throw new ComponentRegistrationException(e);
        }
    }


    private <S, I extends S> LogicalComponent<?> createLogicalComponent(String name, Class<S> type, I instance, boolean introspect)
            throws InvalidServiceContractException, AssemblyException {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        ComponentDefinition<Implementation<?>> definition = createDefinition(name, type, instance, introspect);
        InstantiationContext context = new InstantiationContext();
        LogicalComponent<?> logical = instantiator.instantiate(definition, domain, context);
        logical.setAutowire(domain.getAutowire());
        if (context.hasErrors()) {
            throw new AssemblyException(context.getErrors());
        }
        // mark singleton components as provisioned since instances are not created
        logical.setState(LogicalState.PROVISIONED);
        // all references are initially resolved since they are manually injected
        for (LogicalReference reference : logical.getReferences()) {
            reference.setResolved(true);
            for (LogicalWire wire : reference.getWires()) {
                wire.setState(LogicalState.PROVISIONED);
            }
        }
        return logical;
    }

    private <S, I extends S> ComponentDefinition<Implementation<?>> createDefinition(String name, Class<S> type, I instance, boolean introspect)
            throws InvalidServiceContractException {

        String implClassName = instance.getClass().getName();

        ClassLoader loader = getClass().getClassLoader();
        IntrospectionContext context = new DefaultIntrospectionContext(BOOT_CONTRIBUTION, loader);
        if (introspect) {
            // introspect the instance so it may be injected by the runtime with additional services
            SystemImplementation implementation = new SystemImplementation();
            implementation.setImplementationClass(implClassName);
            implementationProcessor.introspect(implementation, context);
            ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
            SingletonImplementation singletonImplementation = new SingletonImplementation(implementation.getComponentType(), implClassName);
            def.setImplementation(singletonImplementation);
            def.setContributionUri(BOOT_CONTRIBUTION);
            return def;
        } else {
            // instance does not have any services injected
            ServiceContract contract = contractProcessor.introspect(type, context);
            if (context.hasErrors()) {
                throw new InvalidServiceContractException(context.getErrors());
            }
            String serviceName = contract.getInterfaceName();
            ServiceDefinition service = new ServiceDefinition(serviceName, contract);

            InjectingComponentType componentType = new InjectingComponentType(implClassName);
            componentType.add(service);

            SingletonImplementation implementation = new SingletonImplementation(componentType, implClassName);
            implementation.setComponentType(componentType);
            ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
            def.setImplementation(implementation);
            def.setContributionUri(BOOT_CONTRIBUTION);
            return def;
        }
    }

    private <I> AtomicComponent createPhysicalComponent(LogicalComponent<?> logicalComponent, I instance) {
        URI uri = logicalComponent.getUri();
        InjectingComponentType type = (InjectingComponentType) logicalComponent.getDefinition().getComponentType();
        type.getInjectionSites();
        SingletonComponent component = new SingletonComponent(uri, instance, type.getInjectionSites());
        component.setClassLoaderId(BOOT_CONTRIBUTION);
        return component;
    }


}
