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
package org.fabric3.fabric.synthesizer;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.implementation.system.singleton.SingletonComponent;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.system.SystemImplementation;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.host.Names.BOOT_CONTRIBUTION;

/**
 * Implementation that synthesizes a singleton component from an existing object instance.
 */
public class SingletonComponentSynthesizer implements ComponentSynthesizer {

    private ImplementationIntrospector implementationIntrospector;
    private AtomicComponentInstantiator instantiator;
    private LogicalComponentManager lcm;
    private ComponentManager componentManager;
    private JavaContractProcessor contractProcessor;
    private ScopeContainer scopeContainer;

    @Constructor
    public SingletonComponentSynthesizer(@Reference ImplementationIntrospector implementationIntrospector,
                                         @Reference AtomicComponentInstantiator instantiator,
                                         @Reference LogicalComponentManager lcm,
                                         @Reference ComponentManager componentManager,
                                         @Reference JavaContractProcessor contractProcessor,
                                         @Reference ScopeRegistry registry) {
        this(implementationIntrospector, instantiator, lcm, componentManager, contractProcessor, registry.getScopeContainer(Scope.COMPOSITE));
    }

    public SingletonComponentSynthesizer(ImplementationIntrospector implementationIntrospector,
                                         AtomicComponentInstantiator instantiator,
                                         LogicalComponentManager lcm,
                                         ComponentManager componentManager,
                                         JavaContractProcessor contractProcessor,
                                         ScopeContainer scopeContainer) {
        this.implementationIntrospector = implementationIntrospector;
        this.instantiator = instantiator;
        this.lcm = lcm;
        this.componentManager = componentManager;
        this.contractProcessor = contractProcessor;
        this.scopeContainer = scopeContainer;
    }

    public <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws Fabric3Exception {
        LogicalComponent<?> logical = createLogicalComponent(name, type, instance, introspect);
        ScopedComponent physical = createPhysicalComponent(logical, instance);
        componentManager.register(physical);
        scopeContainer.register(physical);
        // initialize the component - needed for reinjection to work
        scopeContainer.getInstance(physical);
    }

    private <S, I extends S> LogicalComponent<?> createLogicalComponent(String name, Class<S> type, I instance, boolean introspect)
            throws InvalidServiceContractException, AssemblyException {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        Component<Implementation<?>> definition = createDefinition(name, type, instance, introspect);
        InstantiationContext context = new InstantiationContext();
        LogicalComponent<?> logical = instantiator.instantiate(definition, domain, context);
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

    private <S, I extends S> Component<Implementation<?>> createDefinition(String name, Class<S> type, I instance, boolean introspect) {

        Class<?> instanceClass = instance.getClass();

        ClassLoader loader = getClass().getClassLoader();
        IntrospectionContext context = new DefaultIntrospectionContext(BOOT_CONTRIBUTION, loader);
        if (introspect) {
            // introspect the instance so it may be injected by the runtime with additional services
            SystemImplementation implementation = new SystemImplementation();
            implementation.setImplementationClass(instance.getClass());
            InjectingComponentType componentType = new InjectingComponentType(instanceClass);
            implementationIntrospector.introspect(componentType, context);
            implementation.setComponentType(componentType);

            Component<Implementation<?>> def = new Component<>(name);
            SingletonImplementation singletonImplementation = new SingletonImplementation(implementation.getComponentType());
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
            Service<ComponentType> service = new Service<>(serviceName, contract);

            InjectingComponentType componentType = new InjectingComponentType(instanceClass);
            componentType.add(service);

            SingletonImplementation implementation = new SingletonImplementation(componentType);
            implementation.setComponentType(componentType);
            Component<Implementation<?>> def = new Component<>(name);
            def.setImplementation(implementation);
            def.setContributionUri(BOOT_CONTRIBUTION);
            return def;
        }
    }

    private <I> ScopedComponent createPhysicalComponent(LogicalComponent<?> logicalComponent, I instance) {
        URI uri = logicalComponent.getUri();
        InjectingComponentType type = (InjectingComponentType) logicalComponent.getDefinition().getComponentType();
        type.getInjectionSites();
        SingletonComponent component = new SingletonComponent(uri, instance, type.getInjectionSites());
        component.setContributionUri(BOOT_CONTRIBUTION);
        return component;
    }

}
