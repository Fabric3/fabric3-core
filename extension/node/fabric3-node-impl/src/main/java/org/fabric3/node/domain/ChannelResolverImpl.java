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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.node.NotFoundException;
import org.fabric3.node.nonmanaged.NonManagedImplementation;
import org.fabric3.node.nonmanaged.NonManagedPhysicalConnectionSourceDefinition;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.ChannelConnector;
import org.fabric3.spi.container.builder.channel.ChannelBuilderRegistry;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ChannelGenerator;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelResolverImpl implements ChannelResolver {
    private static final QName SYNTHETIC_DEPLOYABLE = new QName(HostNamespaces.SYNTHESIZED, "SyntheticDeployable");

    private Introspector introspector;
    private LogicalComponentManager lcm;
    private ChannelGenerator channelGenerator;
    private ConnectionGenerator connectionGenerator;
    private ChannelBuilderRegistry channelBuilderRegistry;
    private ChannelConnector channelConnector;

    public ChannelResolverImpl(@Reference Introspector introspector,
                               @Reference(name = "lcm") LogicalComponentManager lcm,
                               @Reference ChannelGenerator channelGenerator,
                               @Reference ConnectionGenerator connectionGenerator,
                               @Reference ChannelBuilderRegistry channelBuilderRegistry,
                               @Reference ChannelConnector channelConnector) {
        this.introspector = introspector;
        this.lcm = lcm;
        this.channelGenerator = channelGenerator;
        this.connectionGenerator = connectionGenerator;
        this.channelBuilderRegistry = channelBuilderRegistry;
        this.channelConnector = channelConnector;
    }

    public <T> T resolve(Class<T> interfaze, String name) throws ContainerException {
        LogicalChannel logicalChannel = getChannel(name);
        LogicalProducer producer = createProducer(interfaze, logicalChannel.getUri());
        PhysicalChannelDefinition channelDefinition = channelGenerator.generateChannelDefinition(logicalChannel,
                                                                                                 SYNTHETIC_DEPLOYABLE,
                                                                                                 ChannelDirection.PRODUCER);

        channelBuilderRegistry.build(channelDefinition);

        Map<LogicalChannel, ChannelDeliveryType> channels = Collections.singletonMap(logicalChannel, ChannelDeliveryType.DEFAULT);
        List<PhysicalChannelConnectionDefinition> physicalDefinitions = connectionGenerator.generateProducer(producer, channels);
        for (PhysicalChannelConnectionDefinition physicalDefinition : physicalDefinitions) {
            channelConnector.connect(physicalDefinition);
        }
        for (PhysicalChannelConnectionDefinition physicalDefinition : physicalDefinitions) {
            PhysicalConnectionSourceDefinition source = physicalDefinition.getSource();
            if (!(source instanceof NonManagedPhysicalConnectionSourceDefinition)) {
                continue;
            }
            NonManagedPhysicalConnectionSourceDefinition sourceDefinition = (NonManagedPhysicalConnectionSourceDefinition) source;
            return interfaze.cast(sourceDefinition.getProxy());
        }
        throw new GenerationException("Source generator not found");

    }

    private LogicalChannel getChannel(String name) throws ContainerException {
        LogicalCompositeComponent domainComponent = lcm.getRootComponent();
        String domainRoot = domainComponent.getUri().toString();
        URI channelUri = URI.create(domainRoot + "/" + name);
        LogicalChannel logicalChannel = domainComponent.getChannel(channelUri);
        if (logicalChannel == null) {
            throw new NotFoundException("Channel not found: " + name);
        }
        return logicalChannel;
    }

    private <T> LogicalProducer createProducer(Class<T> interfaze, URI channelUri) throws ContainerException {
        JavaServiceContract contract = introspector.introspect(interfaze);

        LogicalCompositeComponent domainComponent = lcm.getRootComponent();
        String domainRoot = domainComponent.getUri().toString();
        URI componentUri = URI.create(domainRoot + "/F3Synthetic");

        InjectingComponentType componentType = new InjectingComponentType();
        NonManagedImplementation implementation = new NonManagedImplementation();
        implementation.setComponentType(componentType);
        Component<NonManagedImplementation> component = new Component<>("F3Synthetic");
        component.setContributionUri(ContributionResolver.getContribution(interfaze));
        component.setImplementation(implementation);
        LogicalComponent<NonManagedImplementation> logicalComponent = new LogicalComponent<>(componentUri, component, domainComponent);

        Producer producerDefinition = new Producer("producer", contract);

        LogicalProducer producer = new LogicalProducer(URI.create(domainRoot + "/F3Synthetic#producer"), producerDefinition, logicalComponent);
        producer.addTarget(channelUri);
        return producer;
    }

}
