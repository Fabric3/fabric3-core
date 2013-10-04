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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.fabric3.host.Names;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.node.nonmanaged.NonManagedImplementation;
import org.fabric3.node.nonmanaged.NonManagedPhysicalConnectionSourceDefinition;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.ChannelConnector;
import org.fabric3.spi.builder.channel.ChannelBuilderRegistry;
import org.fabric3.spi.generator.ChannelDirection;
import org.fabric3.spi.generator.ChannelGenerator;
import org.fabric3.spi.generator.ConnectionGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelResolverImpl implements ChannelResolver {
    private static final QName SYNTHETIC_DEPLOYABLE = new QName(Namespaces.SYNTHESIZED, "SyntheticDeployable");

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

    public <T> T resolve(Class<T> interfaze, String name) throws ResolverException {
        try {
            LogicalChannel logicalChannel = createChannel(name);
            LogicalProducer producer = createProducer(interfaze, logicalChannel.getUri());
            PhysicalChannelDefinition channelDefinition = channelGenerator.generateChannelDefinition(logicalChannel,
                                                                                                     SYNTHETIC_DEPLOYABLE,
                                                                                                     ChannelDirection.PRODUCER);

            Map<LogicalChannel, ChannelDeliveryType> channels = Collections.singletonMap(logicalChannel, ChannelDeliveryType.DEFAULT);
            PhysicalChannelConnectionDefinition physicalDefinition = connectionGenerator.generateProducer(producer, channels).get(0);  // must be 1

            channelBuilderRegistry.build(channelDefinition);

            channelConnector.connect(physicalDefinition);
            NonManagedPhysicalConnectionSourceDefinition sourceDefinition = (NonManagedPhysicalConnectionSourceDefinition) physicalDefinition.getSource();
            return interfaze.cast(sourceDefinition.getProxy());
        } catch (GenerationException e) {
            throw new ResolverException(e);
        } catch (BuilderException e) {
            throw new ResolverException(e);
        }
    }

    private LogicalChannel createChannel(String name) throws ResolverException {
        LogicalCompositeComponent domainComponent = lcm.getRootComponent();
        String domainRoot = domainComponent.getUri().toString();
        URI channelUri = URI.create(domainRoot + "/" + name);
        LogicalChannel logicalChannel = domainComponent.getChannel(channelUri);
        if (logicalChannel == null) {
            throw new ResolverException("Channel not found: " + name);
        }
        return logicalChannel;
    }

    private <T> LogicalProducer createProducer(Class<T> interfaze, URI channelUri) throws InterfaceException {
        JavaServiceContract contract = introspector.introspect(interfaze);

        LogicalCompositeComponent domainComponent = lcm.getRootComponent();
        String domainRoot = domainComponent.getUri().toString();
        URI componentUri = URI.create(domainRoot + "Synthetic");

        InjectingComponentType componentType = new InjectingComponentType();
        NonManagedImplementation implementation = new NonManagedImplementation();
        implementation.setComponentType(componentType);
        ComponentDefinition<NonManagedImplementation> componentDefinition = new ComponentDefinition<NonManagedImplementation>("Synthetic");
        componentDefinition.setContributionUri(Names.HOST_CONTRIBUTION);
        componentDefinition.setImplementation(implementation);
        LogicalComponent<NonManagedImplementation> logicalComponent = new LogicalComponent<NonManagedImplementation>(componentUri,
                                                                                                                     componentDefinition,
                                                                                                                     domainComponent);

        ProducerDefinition producerDefinition = new ProducerDefinition("producer", contract);

        LogicalProducer producer = new LogicalProducer(URI.create(domainRoot + "Synthetic#producer"), producerDefinition, logicalComponent);
        producer.addTarget(channelUri);
        return producer;
    }

}
