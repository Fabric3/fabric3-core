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
package org.fabric3.fabric.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.contribution.generator.JavaContributionWireGenerator;
import org.fabric3.contribution.generator.LocationContributionWireGenerator;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.spi.container.builder.Connector;
import org.fabric3.fabric.container.builder.ConnectorImpl;
import org.fabric3.fabric.domain.Collector;
import org.fabric3.fabric.domain.CollectorImpl;
import org.fabric3.fabric.deployment.command.AttachWireCommand;
import org.fabric3.fabric.deployment.command.BuildComponentCommand;
import org.fabric3.fabric.deployment.command.ConnectionCommand;
import org.fabric3.fabric.deployment.command.StartComponentCommand;
import org.fabric3.fabric.deployment.command.StartContextCommand;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.domain.ContributionHelper;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.LocalDeployer;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.deployment.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.deployment.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.deployment.executor.ConnectionCommandExecutor;
import org.fabric3.fabric.deployment.executor.ContextMonitor;
import org.fabric3.fabric.deployment.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.deployment.executor.StartContextCommandExecutor;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGeneratorImpl;
import org.fabric3.fabric.generator.collator.ContributionCollator;
import org.fabric3.fabric.generator.collator.ContributionCollatorImpl;
import org.fabric3.fabric.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.generator.context.StartContextCommandGeneratorImpl;
import org.fabric3.fabric.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.generator.context.StopContextCommandGeneratorImpl;
import org.fabric3.fabric.generator.impl.GeneratorImpl;
import org.fabric3.fabric.generator.impl.GeneratorRegistryImpl;
import org.fabric3.fabric.generator.policy.NullPolicyAttacher;
import org.fabric3.fabric.generator.policy.NullPolicyResolver;
import org.fabric3.fabric.generator.wire.BoundServiceCommandGenerator;
import org.fabric3.fabric.generator.wire.OperationResolverImpl;
import org.fabric3.fabric.generator.wire.PhysicalOperationGenerator;
import org.fabric3.fabric.generator.wire.PhysicalOperationGeneratorImpl;
import org.fabric3.fabric.generator.wire.ReferenceCommandGenerator;
import org.fabric3.fabric.generator.wire.ResourceReferenceCommandGenerator;
import org.fabric3.fabric.instantiator.wire.TypeAutowireResolver;
import org.fabric3.spi.generator.wire.WireGenerator;
import org.fabric3.fabric.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.AutowireInstantiator;
import org.fabric3.fabric.instantiator.AutowireNormalizer;
import org.fabric3.fabric.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.instantiator.component.AutowireNormalizerImpl;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiatorImpl;
import org.fabric3.fabric.instantiator.promotion.PromotionNormalizerImpl;
import org.fabric3.fabric.instantiator.promotion.PromotionResolutionServiceImpl;
import org.fabric3.fabric.instantiator.wire.AutowireInstantiatorImpl;
import org.fabric3.fabric.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.fabric.xml.XMLFactoryImpl;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.implementation.pojo.builder.ArrayBuilder;
import org.fabric3.implementation.pojo.builder.ArrayBuilderImpl;
import org.fabric3.implementation.pojo.builder.CollectionBuilder;
import org.fabric3.implementation.pojo.builder.CollectionBuilderImpl;
import org.fabric3.implementation.pojo.builder.MapBuilder;
import org.fabric3.implementation.pojo.builder.MapBuilderImpl;
import org.fabric3.implementation.pojo.builder.ObjectBuilder;
import org.fabric3.implementation.pojo.builder.ObjectBuilderImpl;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilderImpl;
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
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.implementation.system.provision.SystemSourceDefinition;
import org.fabric3.implementation.system.provision.SystemTargetDefinition;
import org.fabric3.implementation.system.runtime.SystemComponentBuilder;
import org.fabric3.implementation.system.runtime.SystemSourceWireAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonComponentGenerator;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.implementation.system.singleton.SingletonSourceDefinition;
import org.fabric3.implementation.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonTargetDefinition;
import org.fabric3.implementation.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.monitor.generator.MonitorResourceReferenceGenerator;
import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.monitor.provision.MonitorTargetDefinition;
import org.fabric3.monitor.runtime.MonitorWireAttacher;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.contribution.ClassLoaderWireGenerator;
import org.fabric3.spi.generator.component.ComponentGenerator;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.generator.policy.PolicyAttacher;
import org.fabric3.spi.generator.policy.PolicyResolver;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.xml.XMLFactory;
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
    private static final XMLFactory XML_FACTORY = new XMLFactoryImpl();

    private BootstrapAssemblyFactory() {
    }

    public static Domain createDomain(MonitorProxyService monitorService,
                                      ClassLoaderRegistry classLoaderRegistry,
                                      ScopeRegistry scopeRegistry,
                                      ComponentManager componentManager,
                                      LogicalComponentManager logicalComponentManager,
                                      MetaDataStore metaDataStore,
                                      ManagementService managementService,
                                      HostInfo info) throws InitializationException {

        CommandExecutorRegistry commandRegistry = createCommandExecutorRegistry(monitorService,
                                                                                classLoaderRegistry,
                                                                                scopeRegistry,
                                                                                componentManager,
                                                                                managementService,
                                                                                info);
        DeployerMonitor monitor;
        try {
            monitor = monitorService.createMonitor(DeployerMonitor.class);
        } catch (MonitorCreationException e) {
            throw new InitializationException(e);
        }
        LocalDeployer deployer = new LocalDeployer(commandRegistry, scopeRegistry, monitor);

        PolicyAttacher policyAttacher = new NullPolicyAttacher();
        PolicyResolver policyResolver = new NullPolicyResolver();

        DefaultContractMatcher matcher = new DefaultContractMatcher();
        JavaContractMatcherExtension javaMatcher = new JavaContractMatcherExtension();
        matcher.addMatcherExtension(javaMatcher);

        Generator generator = createGenerator(metaDataStore, policyResolver, matcher);

        LogicalModelInstantiator logicalModelInstantiator = createLogicalModelGenerator(matcher);
        Collector collector = new CollectorImpl();
        ContributionHelper contributionHelper = new ContributionHelperImpl(metaDataStore, info);

        return new RuntimeDomain(metaDataStore,
                                 generator,
                                 logicalModelInstantiator,
                                 policyAttacher,
                                 logicalComponentManager,
                                 deployer,
                                 collector,
                                 contributionHelper,
                                 info);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(ContractMatcher matcher) {
        PromotionResolutionService promotionResolutionService = new PromotionResolutionServiceImpl();
        TypeAutowireResolver resolver = new TypeAutowireResolver(matcher);
        AutowireInstantiator autowireInstantiator = new AutowireInstantiatorImpl(resolver);

        PromotionNormalizer promotionNormalizer = new PromotionNormalizerImpl();
        AutowireNormalizer autowireNormalizer = new AutowireNormalizerImpl();
        AtomicComponentInstantiator atomicInstantiator = new AtomicComponentInstantiatorImpl();

        WireInstantiator wireInstantiator = new WireInstantiatorImpl(matcher);

        CompositeComponentInstantiator compositeInstantiator = new CompositeComponentInstantiatorImpl(atomicInstantiator, wireInstantiator);
        return new LogicalModelInstantiatorImpl(compositeInstantiator,
                                                atomicInstantiator,
                                                wireInstantiator,
                                                autowireInstantiator,
                                                promotionNormalizer,
                                                autowireNormalizer,
                                                promotionResolutionService);
    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(MonitorProxyService monitorService,
                                                                         ClassLoaderRegistry classLoaderRegistry,
                                                                         ScopeRegistry scopeRegistry,
                                                                         ComponentManager componentManager,
                                                                         ManagementService managementService,
                                                                         HostInfo info) {

        DefaultTransformerRegistry transformerRegistry = createTransformerRegistry(classLoaderRegistry);

        Connector connector = createConnector(componentManager, transformerRegistry, classLoaderRegistry, monitorService);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        try {
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

        } catch (MonitorCreationException e) {
            throw new AssertionError(e);
        }

        return commandRegistry;
    }

    @SuppressWarnings({"unchecked"})
    private static BuildComponentCommandExecutor createBuildComponentExecutor(ComponentManager componentManager,
                                                                              ScopeRegistry scopeRegistry,
                                                                              DefaultTransformerRegistry transformerRegistry,
                                                                              ClassLoaderRegistry classLoaderRegistry,
                                                                              ManagementService managementService,
                                                                              HostInfo info) {
        Map<Class<?>, ComponentBuilder> builders = new HashMap<Class<?>, ComponentBuilder>();

        ArrayBuilder arrayBuilder = new ArrayBuilderImpl(transformerRegistry);
        CollectionBuilder collectionBuilder = new CollectionBuilderImpl(transformerRegistry);
        MapBuilder mapBuilder = new MapBuilderImpl(transformerRegistry);
        ObjectBuilder objectBuilder = new ObjectBuilderImpl(transformerRegistry);

        PropertyObjectFactoryBuilder propertyBuilder = new PropertyObjectFactoryBuilderImpl(arrayBuilder, collectionBuilder, mapBuilder, objectBuilder);

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

        builders.put(SystemComponentDefinition.class, builder);
        BuildComponentCommandExecutor executor = new BuildComponentCommandExecutor(componentManager);
        executor.setBuilders(builders);
        return executor;
    }

    private static DefaultTransformerRegistry createTransformerRegistry(ClassLoaderRegistry classLoaderRegistry) {
        DefaultTransformerRegistry transformerRegistry = new DefaultTransformerRegistry();
        List<SingleTypeTransformer<?, ?>> transformers = new ArrayList<SingleTypeTransformer<?, ?>>();
        transformers.add(new Property2StringTransformer());
        transformers.add(new Property2IntegerTransformer());
        transformers.add(new Property2BooleanTransformer());
        transformers.add(new Property2ElementTransformer());
        transformers.add(new Property2QNameTransformer());
        transformers.add(new Property2StreamTransformer(XML_FACTORY));
        transformers.add(new String2ClassTransformer(classLoaderRegistry));
        transformers.add(new String2QNameTransformer());
        transformers.add(new String2IntegerTransformer());
        transformerRegistry.setTransformers(transformers);
        return transformerRegistry;
    }

    private static Connector createConnector(ComponentManager componentManager,
                                             DefaultTransformerRegistry transformerRegistry,
                                             ClassLoaderRegistry classLoaderRegistry,
                                             MonitorProxyService monitorService) {
        Map<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>> sourceAttachers
                = new ConcurrentHashMap<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>>();
        SystemSourceWireAttacher wireAttacher = new SystemSourceWireAttacher(componentManager, transformerRegistry, classLoaderRegistry);
        sourceAttachers.put(SystemSourceDefinition.class, wireAttacher);
        sourceAttachers.put(SingletonSourceDefinition.class, new SingletonSourceWireAttacher(componentManager));

        Map<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>> targetAttachers
                = new ConcurrentHashMap<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>>();
        targetAttachers.put(SingletonTargetDefinition.class, new SingletonTargetWireAttacher(componentManager));
        targetAttachers.put(SystemTargetDefinition.class, new SystemTargetWireAttacher(componentManager, classLoaderRegistry));
        targetAttachers.put(MonitorTargetDefinition.class, new MonitorWireAttacher(monitorService, componentManager, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);
        return connector;
    }

    private static Generator createGenerator(MetaDataStore metaDataStore, PolicyResolver resolver, ContractMatcher matcher) {
        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        ClassLoaderCommandGenerator classLoaderGenerator = createClassLoaderGenerator();
        List<CommandGenerator> commandGenerators = createCommandGenerators(resolver, matcher, generatorRegistry);

        StopContextCommandGenerator stopContextGenerator = new StopContextCommandGeneratorImpl();
        StartContextCommandGenerator startContextGenerator = new StartContextCommandGeneratorImpl();

        ContributionCollator collator = new ContributionCollatorImpl(metaDataStore);

        return new GeneratorImpl(commandGenerators, collator, classLoaderGenerator, startContextGenerator, stopContextGenerator);
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

    private static ClassLoaderCommandGenerator createClassLoaderGenerator() {
        ClassLoaderWireGenerator<?> javaGenerator = new JavaContributionWireGenerator();
        ClassLoaderWireGenerator<?> locationGenerator = new LocationContributionWireGenerator();
        Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators
                = new HashMap<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>>();
        generators.put(JavaContributionWire.class, javaGenerator);
        generators.put(LocationContributionWire.class, locationGenerator);

        return new ClassLoaderCommandGeneratorImpl(generators);
    }

    private static List<CommandGenerator> createCommandGenerators(PolicyResolver resolver, ContractMatcher matcher, GeneratorRegistry generatorRegistry) {

        List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();

        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, 1));

        // command generators for wires
        OperationResolver operationResolver = new OperationResolverImpl();
        PhysicalOperationGenerator operationGenerator = new PhysicalOperationGeneratorImpl(operationResolver, generatorRegistry);
        WireGenerator wireGenerator = new WireGeneratorImpl(generatorRegistry, matcher, resolver, operationGenerator);
        commandGenerators.add(new ReferenceCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new BoundServiceCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceReferenceCommandGenerator(wireGenerator, 2));

        StartComponentCommandGenerator startGenerator = new StartComponentCommandGenerator(3);
        commandGenerators.add(startGenerator);

        return commandGenerators;
    }

}
