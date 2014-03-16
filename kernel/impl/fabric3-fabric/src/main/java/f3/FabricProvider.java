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
package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.builder.WireDefinitionBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.classloader.SerializationServiceImpl;
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.container.binding.BindingHandlerRegistryImpl;
import org.fabric3.fabric.container.builder.ChannelConnectorImpl;
import org.fabric3.fabric.container.builder.ConnectorImpl;
import org.fabric3.fabric.container.builder.channel.ChannelBuilderRegistryImpl;
import org.fabric3.fabric.container.builder.channel.ChannelSourceAttacher;
import org.fabric3.fabric.container.builder.channel.ChannelTargetAttacher;
import org.fabric3.fabric.container.builder.channel.TypeEventFilterBuilder;
import org.fabric3.fabric.container.builder.classloader.ClassLoaderBuilderImpl;
import org.fabric3.fabric.container.builder.classloader.ClassLoaderTrackerImpl;
import org.fabric3.fabric.container.builder.classloader.ClassLoaderWireBuilderImpl;
import org.fabric3.fabric.container.component.scope.DomainScopeContainer;
import org.fabric3.fabric.container.component.scope.StatelessScopeContainer;
import org.fabric3.fabric.container.handler.TransformerHandlerFactoryImpl;
import org.fabric3.fabric.container.interceptor.TransformerInterceptorFactoryImpl;
import org.fabric3.fabric.contract.DefaultContractMatcher;
import org.fabric3.fabric.contract.JavaContractMatcherExtension;
import org.fabric3.fabric.contract.JavaToRemoteContractMatcherExtension;
import org.fabric3.fabric.contract.RemoteToJavaContractMatcherExtension;
import org.fabric3.fabric.domain.executor.AttachChannelConnectionCommandExecutor;
import org.fabric3.fabric.domain.executor.AttachExtensionCommandExecutor;
import org.fabric3.fabric.domain.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.domain.executor.BuildChannelCommandExecutor;
import org.fabric3.fabric.domain.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.domain.executor.BuildResourcesCommandExecutor;
import org.fabric3.fabric.domain.executor.ChannelConnectionCommandExecutor;
import org.fabric3.fabric.domain.executor.ConnectionCommandExecutor;
import org.fabric3.fabric.domain.executor.DetachChannelConnectionCommandExecutor;
import org.fabric3.fabric.domain.executor.DetachExtensionCommandExecutor;
import org.fabric3.fabric.domain.executor.DetachWireCommandExecutor;
import org.fabric3.fabric.domain.executor.DisposeChannelCommandExecutor;
import org.fabric3.fabric.domain.executor.DisposeComponentCommandExecutor;
import org.fabric3.fabric.domain.executor.DisposeResourcesCommandExecutor;
import org.fabric3.fabric.domain.executor.ProvisionClassloaderCommandExecutor;
import org.fabric3.fabric.domain.executor.ProvisionExtensionsCommandExecutor;
import org.fabric3.fabric.domain.executor.ProvisionedExtensionTrackerImpl;
import org.fabric3.fabric.domain.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.domain.executor.StartContextCommandExecutor;
import org.fabric3.fabric.domain.executor.StopComponentCommandExecutor;
import org.fabric3.fabric.domain.executor.StopContextCommandExecutor;
import org.fabric3.fabric.domain.executor.UnProvisionExtensionsCommandExecutor;
import org.fabric3.fabric.domain.executor.UnprovisionClassloaderCommandExecutor;
import org.fabric3.fabric.domain.generator.binding.BindingSelectorImpl;
import org.fabric3.fabric.domain.generator.binding.ConfigurableBindingSelectionStrategy;
import org.fabric3.fabric.domain.generator.channel.ChannelCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ChannelGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ConnectionGeneratorImpl;
import org.fabric3.fabric.domain.generator.channel.ConsumerCommandGenerator;
import org.fabric3.fabric.domain.generator.channel.DefaultChannelGeneratorExtensionImpl;
import org.fabric3.fabric.domain.generator.channel.ProducerCommandGenerator;
import org.fabric3.fabric.domain.generator.classloader.ClassLoaderCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.collator.ContributionCollatorImpl;
import org.fabric3.fabric.domain.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.DisposeComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.component.StopComponentCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StartContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.context.StopContextCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.extension.ExtensionGeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorImpl;
import org.fabric3.fabric.domain.generator.impl.GeneratorRegistryImpl;
import org.fabric3.fabric.domain.generator.policy.DefaultPolicyRegistry;
import org.fabric3.fabric.domain.generator.resource.BuildResourceCommandGenerator;
import org.fabric3.fabric.domain.generator.resource.DisposeResourceCommandGenerator;
import org.fabric3.fabric.domain.generator.resource.DomainResourceCommandGeneratorImpl;
import org.fabric3.fabric.domain.generator.utility.NullInterceptorGenerator;
import org.fabric3.fabric.domain.generator.wire.BoundServiceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.OperationResolverImpl;
import org.fabric3.fabric.domain.generator.wire.PhysicalOperationGeneratorImpl;
import org.fabric3.fabric.domain.generator.wire.ReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.ResourceReferenceCommandGenerator;
import org.fabric3.fabric.domain.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.channel.ChannelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.component.AutowireNormalizerImpl;
import org.fabric3.fabric.domain.instantiator.component.CompositeComponentInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.promotion.PromotionNormalizerImpl;
import org.fabric3.fabric.domain.instantiator.promotion.PromotionResolutionServiceImpl;
import org.fabric3.fabric.domain.instantiator.wire.AutowireInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.wire.TypeAutowireResolver;
import org.fabric3.fabric.domain.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.fabric.domain.collector.CollectorImpl;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.DeployMonitorListener;
import org.fabric3.fabric.domain.DistributedDomain;
import org.fabric3.fabric.domain.LocalDeployer;
import org.fabric3.fabric.domain.LogicalComponentManagerImpl;
import org.fabric3.fabric.federation.addressing.AddressCacheImpl;
import org.fabric3.fabric.host.PortAllocatorImpl;
import org.fabric3.fabric.introspection.ComponentProcessorImpl;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.fabric.model.physical.TypeEventFilterDefinition;
import org.fabric3.fabric.repository.FSArtifactCache;
import org.fabric3.fabric.runtime.event.EventServiceImpl;
import org.fabric3.fabric.security.BasicAuthenticatorImpl;
import org.fabric3.fabric.security.KeyStoreManagerImpl;
import org.fabric3.fabric.synthesizer.SingletonComponentSynthesizer;
import org.fabric3.fabric.transport.TransportService;
import org.fabric3.fabric.xml.DocumentLoaderImpl;
import org.fabric3.fabric.xml.XMLFactoryImpl;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

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

        return compositeBuilder.build();
    }

    private static void addBindingSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(BindingHandlerRegistryImpl.class).build());
    }

    private static void addContractSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DefaultContractMatcher.class).build());
        compositeBuilder.component(newBuilder(JavaContractMatcherExtension.class).build());
        compositeBuilder.component(newBuilder(RemoteToJavaContractMatcherExtension.class).build());
        compositeBuilder.component(newBuilder(JavaToRemoteContractMatcherExtension.class).build());
    }

    private static void addServicesSubsystem(CompositeBuilder compositeBuilder) {

        SystemComponentDefinitionBuilder builder = newBuilder(XMLFactoryImpl.class);
        builder.property("input", "com.ctc.wstx.stax.WstxInputFactory");
        builder.property("output", "com.ctc.wstx.stax.WstxOutputFactory");
        compositeBuilder.component(builder.build());

        compositeBuilder.component(newBuilder(EventServiceImpl.class).build());

        compositeBuilder.component(newBuilder(SerializationServiceImpl.class).build());

        compositeBuilder.component(newBuilder(TransportService.class).build());

        compositeBuilder.component(newBuilder(BasicAuthenticatorImpl.class).build());

        compositeBuilder.component(newBuilder(PortAllocatorImpl.class).build());

        compositeBuilder.component(newBuilder(KeyStoreManagerImpl.class).build());

        compositeBuilder.component(newBuilder(TransformerInterceptorFactoryImpl.class).build());

        compositeBuilder.component(newBuilder(TransformerHandlerFactoryImpl.class).build());

        compositeBuilder.component(newBuilder(AddressCacheImpl.class).build());

        compositeBuilder.component(newBuilder(ComponentProcessorImpl.class).build());

        compositeBuilder.component(newBuilder(FSArtifactCache.class).build());

    }

    private static void addBuilderSubsystem(CompositeBuilder compositeBuilder) {

        compositeBuilder.component(newBuilder(ConnectorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelConnectorImpl.class).build());

        compositeBuilder.component(newBuilder(ClassLoaderBuilderImpl.class).build());

        compositeBuilder.component(newBuilder(ClassLoaderTrackerImpl.class).build());

        compositeBuilder.component(newBuilder(ClassLoaderWireBuilderImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelSourceAttacher.class).key(ChannelSourceDefinition.class.getName()).build());

        compositeBuilder.component(newBuilder(ChannelTargetAttacher.class).key(ChannelTargetDefinition.class.getName()).build());

        compositeBuilder.component(newBuilder(TypeEventFilterBuilder.class).key(TypeEventFilterDefinition.class.getName()).build());

        compositeBuilder.component(newBuilder(ChannelBuilderRegistryImpl.class).build());
    }

    private static void addExecutorSubsystem(CompositeBuilder compositeBuilder) {

        compositeBuilder.component(newBuilder(CommandExecutorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(ProvisionedExtensionTrackerImpl.class).build());

        compositeBuilder.component(newBuilder(ProvisionClassloaderCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(AttachExtensionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(DetachExtensionCommandExecutor.class).build());

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

        compositeBuilder.component(newBuilder(UnprovisionClassloaderCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(ConnectionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(ChannelConnectionCommandExecutor.class).build());

        compositeBuilder.component(newBuilder(ProvisionExtensionsCommandExecutor.class).reference("domain", "RuntimeDomain").build());

        compositeBuilder.component(newBuilder(UnProvisionExtensionsCommandExecutor.class).reference("domain", "RuntimeDomain").build());
    }

    private static void addGeneratorSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(GeneratorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(PhysicalOperationGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(OperationResolverImpl.class).build());

        compositeBuilder.component(newBuilder(GeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(WireGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ConnectionGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ContributionCollatorImpl.class).build());

        compositeBuilder.component(newBuilder(ExtensionGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ClassLoaderCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(DomainResourceCommandGeneratorImpl.class).build());

        compositeBuilder.component(newBuilder(BindingSelectorImpl.class).build());

        compositeBuilder.component(newBuilder(ConfigurableBindingSelectionStrategy.class).build());

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

        SystemComponentDefinitionBuilder componentBuilder = newBuilder("NullInterceptorGenerator", NullInterceptorGenerator.class);
        componentBuilder.key(Namespaces.F3_PREFIX + "allowsPassByReferencePolicy");
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(DefaultChannelGeneratorExtensionImpl.class).key("default").build());

        // the wire forces a reinjection of the generator into the RuntimeDomain
        compositeBuilder.wire(WireDefinitionBuilder.newBuilder().source("RuntimeDomain/generator").target("Generator").build());
    }

    private static void addInstantiatorSubsystem(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(LogicalModelInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(AtomicComponentInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(CompositeComponentInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(WireInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(ChannelInstantiatorImpl.class).build());

        compositeBuilder.component(newBuilder(PromotionNormalizerImpl.class).build());

        compositeBuilder.component(newBuilder(AutowireNormalizerImpl.class).build());

        compositeBuilder.component(newBuilder(PromotionResolutionServiceImpl.class).build());

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
        SystemComponentDefinitionBuilder componentBuilder = newBuilder("ApplicationDomain", DistributedDomain.class);
        componentBuilder.reference("logicalComponentManager", "LogicalComponentManager");
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(DeployMonitorListener.class).build());

        compositeBuilder.component(newBuilder(ContributionHelperImpl.class).build());

        compositeBuilder.component(newBuilder(LogicalComponentManagerImpl.class).build());

        compositeBuilder.component(newBuilder(LocalDeployer.class).build());

        compositeBuilder.component(newBuilder(SingletonComponentSynthesizer.class).build());

        compositeBuilder.component(newBuilder("PolicyRegistry", DefaultPolicyRegistry.class).build());

        compositeBuilder.wire(WireDefinitionBuilder.newBuilder().source("RuntimeDomain/deployer").target("LocalDeployer").build());

        compositeBuilder.wire(WireDefinitionBuilder.newBuilder().source("RuntimeDomain/policyRegistry").target("PolicyRegistry").build());

        compositeBuilder.wire(WireDefinitionBuilder.newBuilder().source("ApplicationDomain/policyRegistry").target("PolicyRegistry").build());
    }
}
