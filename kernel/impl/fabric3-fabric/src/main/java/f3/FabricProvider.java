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
 */
package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.builder.WireBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.container.binding.BindingHandlerRegistryImpl;
import org.fabric3.fabric.container.builder.ChannelConnectorImpl;
import org.fabric3.fabric.container.builder.ConnectorImpl;
import org.fabric3.fabric.container.builder.channel.ChannelBuilderRegistryImpl;
import org.fabric3.fabric.container.builder.channel.ChannelSourceAttacher;
import org.fabric3.fabric.container.builder.channel.ChannelTargetAttacher;
import org.fabric3.fabric.container.channel.TransformerHandlerFactoryImpl;
import org.fabric3.fabric.container.command.AttachChannelConnectionCommandExecutor;
import org.fabric3.fabric.container.command.AttachWireCommandExecutor;
import org.fabric3.fabric.container.command.BuildChannelCommandExecutor;
import org.fabric3.fabric.container.command.BuildComponentCommandExecutor;
import org.fabric3.fabric.container.command.BuildResourcesCommandExecutor;
import org.fabric3.fabric.container.command.ChannelConnectionCommandExecutor;
import org.fabric3.fabric.container.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.container.command.ConnectionCommandExecutor;
import org.fabric3.fabric.container.command.DetachChannelConnectionCommandExecutor;
import org.fabric3.fabric.container.command.DetachWireCommandExecutor;
import org.fabric3.fabric.container.command.DisposeChannelCommandExecutor;
import org.fabric3.fabric.container.command.DisposeComponentCommandExecutor;
import org.fabric3.fabric.container.command.DisposeResourcesCommandExecutor;
import org.fabric3.fabric.container.command.StartComponentCommandExecutor;
import org.fabric3.fabric.container.command.StartContextCommandExecutor;
import org.fabric3.fabric.container.command.StopComponentCommandExecutor;
import org.fabric3.fabric.container.command.StopContextCommandExecutor;
import org.fabric3.fabric.container.component.DomainScopeContainer;
import org.fabric3.fabric.container.component.StatelessScopeContainer;
import org.fabric3.fabric.container.wire.TransformerInterceptorFactoryImpl;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.discovery.ConfigurationRegistryImpl;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.DistributedDomain;
import org.fabric3.fabric.domain.LocalDeployer;
import org.fabric3.fabric.domain.LogicalComponentManagerImpl;
import org.fabric3.fabric.domain.collector.CollectorImpl;
import org.fabric3.fabric.domain.generator.channel.ChannelCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ChannelGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ConnectionGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ConsumerCommandGenerator;
import org.fabric3.fabric.domain.generator.channel.DefaultChannelGeneratorExtensionImpl;
import org.fabric3.fabric.domain.generator.channel.ProducerCommandGenerator;
import org.fabric3.fabric.domain.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.DisposeComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.StopComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StartContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.context.StopContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorRegistryImpl;
import org.fabric3.fabric.domain.generator.resource.BuildResourceCommandGenerator;
import org.fabric3.fabric.domain.generator.resource.DisposeResourceCommandGenerator;
import org.fabric3.fabric.domain.generator.resource.DomainResourceCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.wire.BoundServiceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.OperationResolverImpl;
import org.fabric3.fabric.domain.generator.wire.PhysicalOperationGeneratorImpl;
import org.fabric3.fabric.domain.generator.wire.ReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.ResourceReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.channel.ChannelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.component.CompositeComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.wire.AutowireInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.wire.TypeAutowireResolver;
import org.fabric3.fabric.domain.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.fabric.host.PortAllocatorImpl;
import org.fabric3.fabric.model.physical.ChannelSource;
import org.fabric3.fabric.model.physical.ChannelTarget;
import org.fabric3.fabric.node.ChannelResolverImpl;
import org.fabric3.fabric.node.InjectorFactoryImpl;
import org.fabric3.fabric.node.IntrospectorImpl;
import org.fabric3.fabric.node.NodeDomain;
import org.fabric3.fabric.node.ProvisionerImpl;
import org.fabric3.fabric.node.ServiceResolverImpl;
import org.fabric3.fabric.node.nonmanaged.NonManagedComponentGenerator;
import org.fabric3.fabric.node.nonmanaged.NonManagedComponentSourceWireAttacher;
import org.fabric3.fabric.node.nonmanaged.NonManagedConnectionSourceAttacher;
import org.fabric3.fabric.node.nonmanaged.NonManagedConnectionTargetAttacher;
import org.fabric3.fabric.runtime.event.EventServiceImpl;
import org.fabric3.fabric.security.KeyStoreManagerImpl;
import org.fabric3.fabric.node.BindingServiceIntrospector;
import org.fabric3.fabric.node.ServiceIntrospectorImpl;
import org.fabric3.fabric.synthesizer.SingletonComponentSynthesizer;
import org.fabric3.fabric.transport.TransportService;
import org.fabric3.fabric.xml.DocumentLoaderImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;
import static org.fabric3.spi.model.type.system.SystemComponentBuilder.newBuilder;

/**
 * Provides subsystems for core runtime operation.
 */
public class FabricProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "FabricComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        addDomainSubsystem(compositeBuilder);
        addScopeSubsystem(compositeBuilder);
        addInstantiatorSubsystem(compositeBuilder);
        addGeneratorSubsystem(compositeBuilder);
        addExecutorSubsystem(compositeBuilder);
        addBuilderSubsystem(compositeBuilder);
        addServicesSubsystem(compositeBuilder);
        addContractSubsystem(compositeBuilder);
        addBindingSubsystem(compositeBuilder);

        addNodeSubsystem(compositeBuilder);

        addDiscoverySubsystem(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void addNodeSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder("Domain", NodeDomain.class).reference("domain", "ApplicationDomain").build());
        compositeBuilder.component(newBuilder("Provisioner", ProvisionerImpl.class).reference("domain", "ApplicationDomain").build());
        compositeBuilder.component(newBuilder("ServiceResolver", ServiceResolverImpl.class).reference("lcm", "LogicalComponentManager").build());
        compositeBuilder.component(newBuilder("ChannelResolver", ChannelResolverImpl.class).reference("lcm", "LogicalComponentManager").build());
        compositeBuilder.component(newBuilder(IntrospectorImpl.class).build());
        compositeBuilder.component(newBuilder(NonManagedComponentGenerator.class).key("org.fabric3.fabric.node.nonmanaged.NonManagedImplementation").build());
        compositeBuilder.component(newBuilder(NonManagedComponentSourceWireAttacher.class).key("org.fabric3.fabric.node.nonmanaged.NonManagedWireSource")
                                           .build());
        compositeBuilder.component(newBuilder(NonManagedConnectionSourceAttacher.class).key("org.fabric3.fabric.node.nonmanaged.NonManagedConnectionSource")
                                           .build());
        compositeBuilder.component(newBuilder(NonManagedConnectionTargetAttacher.class).key("org.fabric3.fabric.node.nonmanaged.NonManagedConnectionTarget")
                                           .build());
        compositeBuilder.component(newBuilder(ServiceIntrospectorImpl.class).build());
        compositeBuilder.component(newBuilder(BindingServiceIntrospector.class).build());
        compositeBuilder.component(newBuilder(InjectorFactoryImpl.class).build());
    }

    private static void addBindingSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(BindingHandlerRegistryImpl.class).build());
    }

    private static void addContractSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DefaultContractMatcher.class).build());
        compositeBuilder.component(newBuilder(JavaContractMatcherExtension.class).build());
    }

    private static void addServicesSubsystem(CompositeBuilder compositeBuilder) {

        compositeBuilder.component(newBuilder(EventServiceImpl.class).build());

        compositeBuilder.component(newBuilder(TransportService.class).build());

        compositeBuilder.component(newBuilder(PortAllocatorImpl.class).build());

        compositeBuilder.component(newBuilder(KeyStoreManagerImpl.class).build());

        compositeBuilder.component(newBuilder(TransformerInterceptorFactoryImpl.class).build());

        compositeBuilder.component(newBuilder(TransformerHandlerFactoryImpl.class).build());

    }

    private static void addBuilderSubsystem(CompositeBuilder compositeBuilder) {

        compositeBuilder.component(newBuilder(ConnectorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelConnectorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelSourceAttacher.class).key(ChannelSource.class.getName()).build());

        compositeBuilder.component(newBuilder(ChannelTargetAttacher.class).key(ChannelTarget.class.getName()).build());

        compositeBuilder.component(newBuilder(ChannelBuilderRegistryImpl.class).build());
    }

    private static void addExecutorSubsystem(CompositeBuilder compositeBuilder) {

        compositeBuilder.component(newBuilder(CommandExecutorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(BuildComponentCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DisposeComponentCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(BuildChannelCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DisposeChannelCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(AttachWireCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(AttachChannelConnectionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(StartComponentCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(StartContextCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(StopComponentCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(StopContextCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DetachWireCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DetachChannelConnectionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(BuildResourcesCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DisposeResourcesCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(ConnectionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(ChannelConnectionCommandExecutor.class).build());

    }

    private static void addGeneratorSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(GeneratorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(PhysicalOperationGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(OperationResolverImpl.class).build());

        compositeBuilder.component(newBuilder(GeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(WireGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ConnectionGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(DomainResourceCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(StopComponentCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(BuildResourceCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(BuildComponentCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(ReferenceCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(BoundServiceCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(ProducerCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(ConsumerCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(ResourceReferenceCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(StartComponentCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(DisposeComponentCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(DisposeResourceCommandGenerator.class).build());

        compositeBuilder.component(newBuilder(StartContextCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(StopContextCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(DefaultChannelGeneratorExtensionImpl.class).key("default").build());

        // the wire forces a reinjection of the generator into the RuntimeDomain
        compositeBuilder.wire(WireBuilder.newBuilder().source("RuntimeDomain/generator").target("Generator").build());
    }

    private static void addInstantiatorSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(LogicalModelInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(AtomicComponentInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(CompositeComponentInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(WireInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(TypeAutowireResolver.class).build());

        compositeBuilder.component(newBuilder(AutowireInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(DocumentLoaderImpl.class).build());

        compositeBuilder.component(newBuilder(CollectorImpl.class).build());
    }

    private static void addScopeSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DomainScopeContainer.class).build());
        compositeBuilder.component(newBuilder(StatelessScopeContainer.class).build());
    }

    private static void addDomainSubsystem(CompositeBuilder compositeBuilder) {
        SystemComponentBuilder componentBuilder = newBuilder("ApplicationDomain", DistributedDomain.class);
        componentBuilder.reference("logicalComponentManager", "LogicalComponentManager");
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(ContributionHelperImpl.class).build());

        compositeBuilder.component(newBuilder(LogicalComponentManagerImpl.class).build());

        compositeBuilder.component(newBuilder(LocalDeployer.class).build());

        compositeBuilder.component(newBuilder(SingletonComponentSynthesizer.class).build());

        compositeBuilder.wire(WireBuilder.newBuilder().source("RuntimeDomain/deployer").target("LocalDeployer").build());

    }

    private static void addDiscoverySubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(ConfigurationRegistryImpl.class).build());
    }

}
