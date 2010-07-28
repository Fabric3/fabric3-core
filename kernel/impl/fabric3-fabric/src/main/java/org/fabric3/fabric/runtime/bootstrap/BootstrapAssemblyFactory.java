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
package org.fabric3.fabric.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;

import org.fabric3.contribution.generator.JavaContributionWireGeneratorImpl;
import org.fabric3.contribution.generator.LocationContributionWireGeneratorImpl;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.binding.BindingSelectorImpl;
import org.fabric3.fabric.builder.ChannelConnector;
import org.fabric3.fabric.builder.ChannelConnectorImpl;
import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.channel.ChannelSourceAttacher;
import org.fabric3.fabric.builder.channel.ChannelTargetAttacher;
import org.fabric3.fabric.builder.channel.TypeEventFilterBuilder;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.collector.CollectorImpl;
import org.fabric3.fabric.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.domain.ContributionHelper;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.LocalDeployer;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.executor.AttachChannelConnectionCommandExecutor;
import org.fabric3.fabric.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.executor.BuildChannelsCommandExecutor;
import org.fabric3.fabric.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.executor.ChannelConnectionCommandExecutor;
import org.fabric3.fabric.executor.CommandExecutorRegistryImpl;
import org.fabric3.fabric.executor.ConnectionCommandExecutor;
import org.fabric3.fabric.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.executor.StartContextCommandExecutor;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.fabric.generator.channel.ConnectionGenerator;
import org.fabric3.fabric.generator.channel.ConnectionGeneratorImpl;
import org.fabric3.fabric.generator.channel.ConsumerCommandGenerator;
import org.fabric3.fabric.generator.channel.DomainChannelCommandGenerator;
import org.fabric3.fabric.generator.channel.DomainChannelCommandGeneratorImpl;
import org.fabric3.fabric.generator.channel.ProducerCommandGenerator;
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
import org.fabric3.fabric.generator.wire.BoundServiceCommandGenerator;
import org.fabric3.fabric.generator.wire.OperationResolverImpl;
import org.fabric3.fabric.generator.wire.PhysicalOperationGenerator;
import org.fabric3.fabric.generator.wire.PhysicalOperationGeneratorImpl;
import org.fabric3.fabric.generator.wire.ResourceReferenceCommandGenerator;
import org.fabric3.fabric.generator.wire.WireCommandGenerator;
import org.fabric3.fabric.generator.wire.WireGenerator;
import org.fabric3.fabric.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.AutowireInstantiator;
import org.fabric3.fabric.instantiator.AutowireNormalizer;
import org.fabric3.fabric.instantiator.ChannelInstantiator;
import org.fabric3.fabric.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.channel.ChannelInstantiatorImpl;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.instantiator.component.AutowireNormalizerImpl;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiatorImpl;
import org.fabric3.fabric.instantiator.promotion.PromotionNormalizerImpl;
import org.fabric3.fabric.instantiator.promotion.PromotionResolutionServiceImpl;
import org.fabric3.fabric.instantiator.wire.AutowireInstantiatorImpl;
import org.fabric3.fabric.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.fabric.model.physical.TypeEventFilterDefinition;
import org.fabric3.fabric.policy.NullPolicyAttacher;
import org.fabric3.fabric.policy.NullPolicyResolver;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorCreationException;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.JmxSecurity;
import org.fabric3.implementation.pojo.builder.ChannelProxyService;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilderImpl;
import org.fabric3.implementation.pojo.generator.GenerationHelperImpl;
import org.fabric3.implementation.pojo.proxy.JDKChannelProxyService;
import org.fabric3.implementation.pojo.reflection.ReflectiveInstanceFactoryBuilder;
import org.fabric3.implementation.system.generator.SystemComponentGenerator;
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionSourceDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionTargetDefinition;
import org.fabric3.implementation.system.provision.SystemSourceDefinition;
import org.fabric3.implementation.system.provision.SystemTargetDefinition;
import org.fabric3.implementation.system.runtime.SystemComponentBuilder;
import org.fabric3.implementation.system.runtime.SystemSourceConnectionAttacher;
import org.fabric3.implementation.system.runtime.SystemSourceWireAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetConnectionAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonComponentGenerator;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.implementation.system.singleton.SingletonSourceDefinition;
import org.fabric3.implementation.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonTargetDefinition;
import org.fabric3.implementation.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.jmx.JMXManagementService;
import org.fabric3.monitor.generator.MonitorResourceReferenceGenerator;
import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.monitor.provision.MonitorTargetDefinition;
import org.fabric3.monitor.runtime.MonitorWireAttacher;
import org.fabric3.spi.builder.channel.EventFilterBuilder;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventFilterDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.policy.PolicyAttacher;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.property.Property2BooleanTransformer;
import org.fabric3.transform.property.Property2IntegerTransformer;
import org.fabric3.transform.property.Property2QNameTransformer;
import org.fabric3.transform.property.Property2StringTransformer;
import org.fabric3.transform.string2java.String2ClassTransformer;
import org.fabric3.transform.string2java.String2IntegerTransformer;
import org.fabric3.transform.string2java.String2QNameTransformer;

import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL_URI;

/**
 * Bootstraps services required for instantiation, generation, and deployment.
 *
 * @version $Rev$ $Date$
 */
public class BootstrapAssemblyFactory {

    private BootstrapAssemblyFactory() {
    }

    public static Domain createDomain(MonitorProxyService monitorService,
                                      ClassLoaderRegistry classLoaderRegistry,
                                      ScopeRegistry scopeRegistry,
                                      ComponentManager componentManager,
                                      LogicalComponentManager logicalComponentManager,
                                      ChannelManager channelManager,
                                      MetaDataStore metaDataStore,
                                      MBeanServer mBeanServer,
                                      JmxSecurity security,
                                      HostInfo info) throws InitializationException {

        CommandExecutorRegistry commandRegistry = createCommandExecutorRegistry(monitorService,
                                                                                classLoaderRegistry,
                                                                                scopeRegistry,
                                                                                componentManager,
                                                                                channelManager,
                                                                                mBeanServer,
                                                                                security,
                                                                                info);
        DeployerMonitor monitor;
        try {
            monitor = monitorService.createMonitor(DeployerMonitor.class, RUNTIME_MONITOR_CHANNEL_URI);
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

        BindingSelector bindingSelector = new BindingSelectorImpl();

        return new RuntimeDomain(metaDataStore,
                                 generator,
                                 logicalModelInstantiator,
                                 policyAttacher,
                                 logicalComponentManager,
                                 bindingSelector,
                                 deployer,
                                 collector,
                                 contributionHelper,
                                 info);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(ContractMatcher matcher) {
        PromotionResolutionService promotionResolutionService = new PromotionResolutionServiceImpl();
        AutowireInstantiator autowireInstantiator = new AutowireInstantiatorImpl(matcher);

        PromotionNormalizer promotionNormalizer = new PromotionNormalizerImpl();
        AutowireNormalizer autowireNormalizer = new AutowireNormalizerImpl();
        AtomicComponentInstantiator atomicInstantiator = new AtomicComponentInstantiatorImpl();

        WireInstantiator wireInstantiator = new WireInstantiatorImpl(matcher);
        ChannelInstantiator channelInstantiator = new ChannelInstantiatorImpl();

        CompositeComponentInstantiator compositeInstantiator = new CompositeComponentInstantiatorImpl(atomicInstantiator,
                                                                                                      wireInstantiator,
                                                                                                      channelInstantiator);
        return new LogicalModelInstantiatorImpl(compositeInstantiator,
                                                atomicInstantiator,
                                                wireInstantiator,
                                                autowireInstantiator,
                                                channelInstantiator,
                                                promotionNormalizer,
                                                autowireNormalizer,
                                                promotionResolutionService);
    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(MonitorProxyService monitorService,
                                                                         ClassLoaderRegistry classLoaderRegistry,
                                                                         ScopeRegistry scopeRegistry,
                                                                         ComponentManager componentManager,
                                                                         ChannelManager channelManager,
                                                                         MBeanServer mBeanServer,
                                                                         JmxSecurity security,
                                                                         HostInfo info) {

        DefaultTransformerRegistry transformerRegistry = createTransformerRegistry(classLoaderRegistry);

        Connector connector = createConnector(componentManager, transformerRegistry, classLoaderRegistry, monitorService);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        commandRegistry.register(StartContextCommand.class, new StartContextCommandExecutor(scopeRegistry));
        BuildComponentCommandExecutor executor =
                createBuildComponentExecutor(componentManager, scopeRegistry, transformerRegistry, classLoaderRegistry, mBeanServer, security, info);
        commandRegistry.register(BuildComponentCommand.class, executor);
        commandRegistry.register(AttachWireCommand.class, new AttachWireCommandExecutor(connector));
        commandRegistry.register(StartComponentCommand.class, new StartComponentCommandExecutor(componentManager));
        commandRegistry.register(ConnectionCommand.class, new ConnectionCommandExecutor(commandRegistry));
        commandRegistry.register(ChannelConnectionCommand.class, new ChannelConnectionCommandExecutor(commandRegistry));
        commandRegistry.register(BuildChannelsCommand.class, new BuildChannelsCommandExecutor(channelManager, null, commandRegistry));

        ChannelConnector channelConnector = createChannelConnector(componentManager, channelManager, classLoaderRegistry);
        commandRegistry.register(AttachChannelConnectionCommand.class, new AttachChannelConnectionCommandExecutor(commandRegistry, channelConnector));

        return commandRegistry;
    }

    @SuppressWarnings({"unchecked"})
    private static BuildComponentCommandExecutor createBuildComponentExecutor(ComponentManager componentManager,
                                                                              ScopeRegistry scopeRegistry,
                                                                              DefaultTransformerRegistry transformerRegistry,
                                                                              ClassLoaderRegistry classLoaderRegistry,
                                                                              MBeanServer mBeanServer,
                                                                              JmxSecurity security,
                                                                              HostInfo info) {
        Map<Class<?>, ComponentBuilder> builders = new HashMap<Class<?>, ComponentBuilder>();
        PropertyObjectFactoryBuilder propertyBuilder = new PropertyObjectFactoryBuilderImpl(transformerRegistry);
        IntrospectionHelper helper = new DefaultIntrospectionHelper();

        ReflectiveInstanceFactoryBuilder factoryBuilder = new ReflectiveInstanceFactoryBuilder(classLoaderRegistry);
        JMXManagementService managementService = new JMXManagementService(mBeanServer, info, security);
        SystemComponentBuilder builder = new SystemComponentBuilder(scopeRegistry,
                                                                    factoryBuilder,
                                                                    classLoaderRegistry,
                                                                    propertyBuilder,
                                                                    managementService,
                                                                    helper);

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
        transformers.add(new Property2QNameTransformer());
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
        Map<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>> sourceAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>>();
        SystemSourceWireAttacher wireAttacher = new SystemSourceWireAttacher(componentManager, transformerRegistry, classLoaderRegistry);
        sourceAttachers.put(SystemSourceDefinition.class, wireAttacher);
        sourceAttachers.put(SingletonSourceDefinition.class, new SingletonSourceWireAttacher(componentManager));

        Map<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>> targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>>();
        targetAttachers.put(SingletonTargetDefinition.class, new SingletonTargetWireAttacher(componentManager));
        targetAttachers.put(SystemTargetDefinition.class, new SystemTargetWireAttacher(componentManager, classLoaderRegistry));
        targetAttachers.put(MonitorTargetDefinition.class, new MonitorWireAttacher(monitorService, componentManager, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);
        return connector;
    }

    private static ChannelConnector createChannelConnector(ComponentManager componentManager,
                                                           ChannelManager channelManager,
                                                           ClassLoaderRegistry classLoaderRegistry) {
        Map<Class<? extends PhysicalConnectionSourceDefinition>,
                SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>> sourceConnectionAttachers =
                new HashMap<Class<? extends PhysicalConnectionSourceDefinition>,
                        SourceConnectionAttacher<? extends PhysicalConnectionSourceDefinition>>();
        Map<Class<? extends PhysicalConnectionTargetDefinition>,
                TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>> targetConnectionAttachers =
                new HashMap<Class<? extends PhysicalConnectionTargetDefinition>,
                        TargetConnectionAttacher<? extends PhysicalConnectionTargetDefinition>>();

        Map<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>> filterBuilders =
                new HashMap<Class<? extends PhysicalEventFilterDefinition>, EventFilterBuilder<? extends PhysicalEventFilterDefinition>>();

        ChannelSourceAttacher channelSourceAttacher = new ChannelSourceAttacher(channelManager);

        ChannelProxyService proxyService = new JDKChannelProxyService(classLoaderRegistry);

        sourceConnectionAttachers.put(ChannelSourceDefinition.class, channelSourceAttacher);
        SystemSourceConnectionAttacher systemSourceAttacher = new SystemSourceConnectionAttacher(componentManager, proxyService, classLoaderRegistry);
        sourceConnectionAttachers.put(SystemConnectionSourceDefinition.class, systemSourceAttacher);
        ChannelTargetAttacher channelTargetAttacher = new ChannelTargetAttacher(channelManager);
        targetConnectionAttachers.put(ChannelTargetDefinition.class, channelTargetAttacher);
        SystemTargetConnectionAttacher systemTargetAttacher = new SystemTargetConnectionAttacher(componentManager, classLoaderRegistry);
        targetConnectionAttachers.put(SystemConnectionTargetDefinition.class, systemTargetAttacher);

        TypeEventFilterBuilder filterBuilder = new TypeEventFilterBuilder();
        filterBuilders.put(TypeEventFilterDefinition.class, filterBuilder);

        ChannelConnectorImpl channelConnector = new ChannelConnectorImpl();
        channelConnector.setSourceAttachers(sourceConnectionAttachers);
        channelConnector.setTargetAttachers(targetConnectionAttachers);
        channelConnector.setFilterBuilders(filterBuilders);
        return channelConnector;
    }

    private static Generator createGenerator(MetaDataStore metaDataStore, PolicyResolver resolver, ContractMatcher matcher) {
        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        ClassLoaderCommandGenerator classLoaderGenerator = createClassLoaderGenerator();
        List<CommandGenerator> commandGenerators = createCommandGenerators(resolver, matcher, generatorRegistry);
        DomainChannelCommandGenerator channelGenerator = new DomainChannelCommandGeneratorImpl(generatorRegistry);

        StopContextCommandGenerator stopContextGenerator = new StopContextCommandGeneratorImpl();
        StartContextCommandGenerator startContextGenerator = new StartContextCommandGeneratorImpl();

        ContributionCollator collator = new ContributionCollatorImpl(metaDataStore);

        return new GeneratorImpl(commandGenerators, collator, classLoaderGenerator, channelGenerator, startContextGenerator, stopContextGenerator);
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
        ClassLoaderWireGenerator<?> javaGenerator = new JavaContributionWireGeneratorImpl();
        ClassLoaderWireGenerator<?> locationGenerator = new LocationContributionWireGeneratorImpl();
        Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators =
                new HashMap<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>>();
        generators.put(JavaContributionWire.class, javaGenerator);
        generators.put(LocationContributionWire.class, locationGenerator);

        return new ClassLoaderCommandGeneratorImpl(generators);
    }

    private static List<CommandGenerator> createCommandGenerators(PolicyResolver resolver,
                                                                  ContractMatcher matcher,
                                                                  GeneratorRegistry generatorRegistry) {

        List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();

        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, 1));

        // command generators for wires
        OperationResolver operationResolver = new OperationResolverImpl();
        PhysicalOperationGenerator operationGenerator = new PhysicalOperationGeneratorImpl(operationResolver, generatorRegistry);
        WireGenerator wireGenerator = new WireGeneratorImpl(generatorRegistry, matcher, resolver, operationGenerator);
        commandGenerators.add(new WireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new BoundServiceCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceReferenceCommandGenerator(wireGenerator, 2));

        // eventing command generators
        ConnectionGenerator connectionGenerator = new ConnectionGeneratorImpl(generatorRegistry);
        ConsumerCommandGenerator consumerCommandGenerator = new ConsumerCommandGenerator(connectionGenerator, 2);
        commandGenerators.add(consumerCommandGenerator);
        ProducerCommandGenerator producerCommandGenerator = new ProducerCommandGenerator(connectionGenerator, 2);
        commandGenerators.add(producerCommandGenerator);

        StartComponentCommandGenerator startGenerator = new StartComponentCommandGenerator(3);
        commandGenerators.add(startGenerator);

        return commandGenerators;
    }

}
