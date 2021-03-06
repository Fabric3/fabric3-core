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
package org.fabric3.fabric.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.fabric.container.builder.ConnectorImpl;
import org.fabric3.fabric.container.command.AttachWireCommand;
import org.fabric3.fabric.container.command.BuildComponentCommand;
import org.fabric3.fabric.container.command.ConnectionCommand;
import org.fabric3.fabric.container.command.StartComponentCommand;
import org.fabric3.fabric.container.command.StartContextCommand;
import org.fabric3.fabric.container.command.AttachWireCommandExecutor;
import org.fabric3.fabric.container.command.BuildComponentCommandExecutor;
import org.fabric3.fabric.container.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.container.command.ConnectionCommandExecutor;
import org.fabric3.fabric.container.command.ContextMonitor;
import org.fabric3.fabric.container.command.StartComponentCommandExecutor;
import org.fabric3.fabric.container.command.StartContextCommandExecutor;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.domain.ContributionHelper;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.LocalDeployer;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.collector.CollectorImpl;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.fabric.domain.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StartContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StopContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorRegistryImpl;
import org.fabric3.fabric.domain.generator.wire.BoundServiceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.OperationResolverImpl;
import org.fabric3.fabric.domain.generator.wire.PhysicalOperationGenerator;
import org.fabric3.fabric.domain.generator.wire.PhysicalOperationGeneratorImpl;
import org.fabric3.fabric.domain.generator.wire.ReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.ResourceReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.AutowireInstantiator;
import org.fabric3.fabric.domain.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.WireInstantiator;
import org.fabric3.fabric.domain.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.component.CompositeComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.wire.AutowireInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.wire.TypeAutowireResolver;
import org.fabric3.fabric.domain.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.implementation.pojo.builder.ArrayBuilder;
import org.fabric3.implementation.pojo.builder.ArrayBuilderImpl;
import org.fabric3.implementation.pojo.builder.CollectionBuilder;
import org.fabric3.implementation.pojo.builder.CollectionBuilderImpl;
import org.fabric3.implementation.pojo.builder.MapBuilder;
import org.fabric3.implementation.pojo.builder.MapBuilderImpl;
import org.fabric3.implementation.pojo.builder.ObjectBuilder;
import org.fabric3.implementation.pojo.builder.ObjectBuilderImpl;
import org.fabric3.implementation.pojo.builder.PropertySupplierBuilder;
import org.fabric3.implementation.pojo.builder.PropertySupplierBuilderImpl;
import org.fabric3.implementation.pojo.generator.GenerationHelperImpl;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilderImpl;
import org.fabric3.implementation.pojo.reflection.ReflectionFactoryImpl;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.reflection.jdk.JDKConsumerInvokerFactory;
import org.fabric3.implementation.reflection.jdk.JDKInjectorFactory;
import org.fabric3.implementation.reflection.jdk.JDKInstantiatorFactory;
import org.fabric3.implementation.reflection.jdk.JDKLifecycleInvokerFactory;
import org.fabric3.implementation.reflection.jdk.JDKServiceInvokerFactory;
import org.fabric3.implementation.system.generator.SystemComponentGenerator;
import org.fabric3.implementation.system.provision.PhysicalSystemComponent;
import org.fabric3.implementation.system.provision.SystemWireSource;
import org.fabric3.implementation.system.provision.SystemWireTarget;
import org.fabric3.implementation.system.runtime.SystemComponentBuilder;
import org.fabric3.implementation.system.runtime.SystemSourceWireAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonComponentGenerator;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.implementation.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonWireSource;
import org.fabric3.implementation.system.singleton.SingletonWireTarget;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.monitor.generator.MonitorResourceReferenceGenerator;
import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.monitor.provision.MonitorWireTarget;
import org.fabric3.monitor.runtime.MonitorWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.fabric.container.builder.Connector;
import org.fabric3.spi.container.builder.ComponentBuilder;
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.fabric.container.command.CommandExecutorRegistry;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.fabric.domain.LogicalComponentManager;
import org.fabric3.fabric.domain.generator.Generator;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.fabric.domain.generator.wire.WireGenerator;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.type.system.SystemImplementation;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.property.Property2BooleanTransformer;
import org.fabric3.transform.property.Property2ElementTransformer;
import org.fabric3.transform.property.Property2IntegerTransformer;
import org.fabric3.transform.property.Property2QNameTransformer;
import org.fabric3.transform.property.Property2StreamTransformer;
import org.fabric3.transform.property.Property2StringTransformer;
import org.fabric3.transform.string2java.String2ClassTransformer;
import org.fabric3.transform.string2java.String2IntegerTransformer;
import org.fabric3.transform.string2java.String2QNameTransformer;

/**
 * Bootstraps services required for instantiation, generation, and deployment.
 */
public class BootstrapAssemblyFactory {

    private BootstrapAssemblyFactory() {
    }

    public static Domain createDomain(MonitorProxyService monitorService,
                                      ClassLoaderRegistry classLoaderRegistry,
                                      ScopeRegistry scopeRegistry,
                                      ComponentManager componentManager,
                                      LogicalComponentManager logicalComponentManager,
                                      MetaDataStore metaDataStore,
                                      ManagementService managementService,
                                      HostInfo info) throws Fabric3Exception {

        CommandExecutorRegistry commandRegistry = createCommandExecutorRegistry(monitorService,
                                                                                classLoaderRegistry,
                                                                                scopeRegistry,
                                                                                componentManager,
                                                                                managementService,
                                                                                info);
        LocalDeployer deployer = new LocalDeployer(commandRegistry, scopeRegistry);

        DefaultContractMatcher matcher = new DefaultContractMatcher();
        JavaContractMatcherExtension javaMatcher = new JavaContractMatcherExtension();
        matcher.addMatcherExtension(javaMatcher);

        Generator generator = createGenerator(matcher, classLoaderRegistry);

        LogicalModelInstantiator logicalModelInstantiator = createLogicalModelGenerator(matcher);
        Collector collector = new CollectorImpl();
        ContributionHelper contributionHelper = new ContributionHelperImpl(metaDataStore, info);

        return new RuntimeDomain(metaDataStore, generator, logicalModelInstantiator, logicalComponentManager, deployer, collector, contributionHelper, info);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(ContractMatcher matcher) {
        TypeAutowireResolver resolver = new TypeAutowireResolver(matcher);
        AutowireInstantiator autowireInstantiator = new AutowireInstantiatorImpl(resolver);

        AtomicComponentInstantiator atomicInstantiator = new AtomicComponentInstantiatorImpl();

        WireInstantiator wireInstantiator = new WireInstantiatorImpl(matcher);

        CompositeComponentInstantiator compositeInstantiator = new CompositeComponentInstantiatorImpl(atomicInstantiator, wireInstantiator);
        return new LogicalModelInstantiatorImpl(compositeInstantiator, atomicInstantiator, wireInstantiator, autowireInstantiator);
    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(MonitorProxyService monitorService,
                                                                         ClassLoaderRegistry classLoaderRegistry,
                                                                         ScopeRegistry scopeRegistry,
                                                                         ComponentManager componentManager,
                                                                         ManagementService managementService,
                                                                         HostInfo info) {

        DefaultTransformerRegistry transformerRegistry = createTransformerRegistry();

        Connector connector = createConnector(componentManager, transformerRegistry, monitorService);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        ContextMonitor contextMonitor = monitorService.createMonitor(ContextMonitor.class);
        commandRegistry.register(StartContextCommand.class, new StartContextCommandExecutor(scopeRegistry, contextMonitor));
        BuildComponentCommandExecutor executor = createBuildComponentExecutor(componentManager,
                                                                              scopeRegistry,
                                                                              transformerRegistry,
                                                                              classLoaderRegistry,
                                                                              managementService,
                                                                              info);
        commandRegistry.register(BuildComponentCommand.class, executor);
        commandRegistry.register(AttachWireCommand.class, new AttachWireCommandExecutor(connector));
        commandRegistry.register(StartComponentCommand.class, new StartComponentCommandExecutor(componentManager));
        commandRegistry.register(ConnectionCommand.class, new ConnectionCommandExecutor(componentManager, commandRegistry));

        return commandRegistry;
    }

    @SuppressWarnings({"unchecked"})
    private static BuildComponentCommandExecutor createBuildComponentExecutor(ComponentManager componentManager,
                                                                              ScopeRegistry scopeRegistry,
                                                                              DefaultTransformerRegistry transformerRegistry,
                                                                              ClassLoaderRegistry classLoaderRegistry,
                                                                              ManagementService managementService,
                                                                              HostInfo info) {
        Map<Class<?>, ComponentBuilder> builders = new HashMap<>();

        ArrayBuilder arrayBuilder = new ArrayBuilderImpl(transformerRegistry);
        CollectionBuilder collectionBuilder = new CollectionBuilderImpl(transformerRegistry);
        MapBuilder mapBuilder = new MapBuilderImpl(transformerRegistry);
        ObjectBuilder objectBuilder = new ObjectBuilderImpl(transformerRegistry);

        PropertySupplierBuilder propertyBuilder = new PropertySupplierBuilderImpl(arrayBuilder, collectionBuilder, mapBuilder, objectBuilder);

        IntrospectionHelper helper = new DefaultIntrospectionHelper();

        JDKInstantiatorFactory instantiatorFactory = new JDKInstantiatorFactory();
        JDKInjectorFactory injectorFactory = new JDKInjectorFactory();
        JDKLifecycleInvokerFactory lifecycleInvokerFactory = new JDKLifecycleInvokerFactory();
        JDKServiceInvokerFactory serviceInvokerFactory = new JDKServiceInvokerFactory();
        JDKConsumerInvokerFactory consumerInvokerFactory = new JDKConsumerInvokerFactory();
        ReflectionFactory reflectionFactory = new ReflectionFactoryImpl(instantiatorFactory,
                                                                        injectorFactory,
                                                                        lifecycleInvokerFactory,
                                                                        serviceInvokerFactory,
                                                                        consumerInvokerFactory);

        ImplementationManagerFactoryBuilderImpl factoryBuilder = new ImplementationManagerFactoryBuilderImpl(reflectionFactory, classLoaderRegistry);
        SystemComponentBuilder builder = new SystemComponentBuilder(scopeRegistry,
                                                                    factoryBuilder,
                                                                    classLoaderRegistry,
                                                                    propertyBuilder,
                                                                    managementService,
                                                                    helper,
                                                                    info);

        builders.put(PhysicalSystemComponent.class, builder);
        BuildComponentCommandExecutor executor = new BuildComponentCommandExecutor(componentManager);
        executor.setBuilders(builders);
        return executor;
    }

    private static DefaultTransformerRegistry createTransformerRegistry() {
        DefaultTransformerRegistry transformerRegistry = new DefaultTransformerRegistry();
        List<SingleTypeTransformer<?, ?>> transformers = new ArrayList<>();
        transformers.add(new Property2StringTransformer());
        transformers.add(new Property2IntegerTransformer());
        transformers.add(new Property2BooleanTransformer());
        transformers.add(new Property2ElementTransformer());
        transformers.add(new Property2QNameTransformer());
        transformers.add(new Property2StreamTransformer());
        transformers.add(new String2ClassTransformer());
        transformers.add(new String2QNameTransformer());
        transformers.add(new String2IntegerTransformer());
        transformerRegistry.setTransformers(transformers);
        return transformerRegistry;
    }

    private static Connector createConnector(ComponentManager componentManager,
                                             DefaultTransformerRegistry transformerRegistry,
                                             MonitorProxyService monitorService) {
        Map<Class<?>, SourceWireAttacher<?>> sourceAttachers = new HashMap<>();
        SystemSourceWireAttacher wireAttacher = new SystemSourceWireAttacher(componentManager, transformerRegistry);
        sourceAttachers.put(SystemWireSource.class, wireAttacher);
        sourceAttachers.put(SingletonWireSource.class, new SingletonSourceWireAttacher(componentManager));

        Map<Class<?>, TargetWireAttacher<?>> targetAttachers = new HashMap<>();
        targetAttachers.put(SingletonWireTarget.class, new SingletonTargetWireAttacher(componentManager));
        targetAttachers.put(SystemWireTarget.class, new SystemTargetWireAttacher(componentManager));
        targetAttachers.put(MonitorWireTarget.class, new MonitorWireAttacher(monitorService, componentManager));

        return new ConnectorImpl(sourceAttachers, targetAttachers);
    }

    private static Generator createGenerator(ContractMatcher matcher, ClassLoaderRegistry classLoaderRegistry) {
        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        List<CommandGenerator> commandGenerators = createCommandGenerators(matcher, generatorRegistry, classLoaderRegistry);

        StopContextCommandGenerator stopContextGenerator = new StopContextCommandGeneratorImpl();
        StartContextCommandGenerator startContextGenerator = new StartContextCommandGeneratorImpl();

        return new GeneratorImpl(commandGenerators, startContextGenerator, stopContextGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private static GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        GenerationHelperImpl helper = new GenerationHelperImpl();
        ComponentGenerator systemComponentGenerator = new SystemComponentGenerator(helper);
        ComponentGenerator singletonComponentGenerator = new SingletonComponentGenerator();
        registry.register(SystemImplementation.class, systemComponentGenerator);
        registry.register(SingletonImplementation.class, singletonComponentGenerator);
        registry.register(MonitorResourceReference.class, new MonitorResourceReferenceGenerator());
        return registry;
    }

    private static List<CommandGenerator> createCommandGenerators(ContractMatcher matcher,
                                                                  GeneratorRegistry generatorRegistry,
                                                                  ClassLoaderRegistry classLoaderRegistry) {

        List<CommandGenerator> commandGenerators = new ArrayList<>();

        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, classLoaderRegistry));

        // command generators for wires
        OperationResolver operationResolver = new OperationResolverImpl();
        PhysicalOperationGenerator operationGenerator = new PhysicalOperationGeneratorImpl(operationResolver, generatorRegistry);
        WireGenerator wireGenerator = new WireGeneratorImpl(generatorRegistry, matcher, operationGenerator, classLoaderRegistry);
        commandGenerators.add(new ReferenceCommandGenerator(wireGenerator));
        commandGenerators.add(new BoundServiceCommandGenerator(wireGenerator));
        commandGenerators.add(new ResourceReferenceCommandGenerator(wireGenerator));

        StartComponentCommandGenerator startGenerator = new StartComponentCommandGenerator();
        commandGenerators.add(startGenerator);

        return commandGenerators;
    }

}
