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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.binding.jms.spi.generator.JmsResourceProvisioner;
import org.fabric3.binding.jms.spi.provision.JmsChannelBindingDefinition;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsConnectionTargetDefinition;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.spi.model.physical.ChannelConstants.DURABLE_INTENT;
import static org.fabric3.spi.model.physical.ChannelConstants.NON_PERSISTENT_INTENT;

/**
 * Connection binding generator that creates source and target definitions for bound channels, producers, and consumers.
 */
@EagerInit
public class JmsConnectionBindingGenerator implements ConnectionBindingGenerator<JmsBindingDefinition> {
    private static final String JAXB = "JAXB";

    /**
     * Indicates consumers on a channel will receive messages using CLIENT_ACKNOWLEDGE mode
     */
    private static final QName CLIENT_ACKNOWLEDGE_INTENT = new QName(org.fabric3.api.Namespaces.F3, "clientAcknowledge");

    // optional provisioner for host runtimes to receive callbacks
    private JmsResourceProvisioner provisioner;

    @Reference(required = false)
    public void setProvisioner(JmsResourceProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer,
                                                                       LogicalBinding<JmsBindingDefinition> binding,
                                                                       ChannelDeliveryType deliveryType) throws GenerationException {
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        generateIntents(binding, metadata);
        SessionType sessionType = getSessionType(binding);

        JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getConnectionFactory(), sessionType);
        URI uri = consumer.getUri();

        // set the client id specifier
        if (metadata.getSubscriptionId() == null && metadata.isDurable()) {
            metadata.setSubscriptionId(JmsGeneratorHelper.getSubscriptionId(uri));
        }
        String specifier = metadata.getSubscriptionId();
        metadata.setSubscriptionId(specifier);

        metadata.getDestination().setType(DestinationType.TOPIC);  // only use topics for channels
        DataType dataType = isJAXB(consumer.getDefinition().getTypes()) ? PhysicalDataTypes.JAXB : PhysicalDataTypes.JAVA_TYPE;
        JmsConnectionSourceDefinition definition = new JmsConnectionSourceDefinition(uri, metadata, dataType, sessionType);
        if (provisioner != null) {
            provisioner.generateConnectionSource(definition);
        }
        return definition;
    }

    private SessionType getSessionType(LogicalBinding<JmsBindingDefinition> binding) {
        return binding.getDefinition().getIntents().contains(CLIENT_ACKNOWLEDGE_INTENT) ? SessionType.CLIENT_ACKNOWLEDGE : SessionType.AUTO_ACKNOWLEDGE;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer,
                                                                       LogicalBinding<JmsBindingDefinition> binding,
                                                                       ChannelDeliveryType deliveryType) throws GenerationException {
        URI uri = binding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        generateIntents(binding, metadata);

        JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getConnectionFactory(), SessionType.AUTO_ACKNOWLEDGE);

        DataType type = isJAXB(producer.getStreamOperation().getDefinition().getInputTypes()) ? PhysicalDataTypes.JAXB : PhysicalDataTypes.JAVA_TYPE;

        JmsConnectionTargetDefinition definition = new JmsConnectionTargetDefinition(uri, metadata, type);
        if (provisioner != null) {
            provisioner.generateConnectionTarget(definition);
        }
        return definition;
    }

    public PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<JmsBindingDefinition> binding, ChannelDeliveryType deliveryType)
            throws GenerationException {
        // a binding definition needs to be created even though it is not used so the channel is treated as bound (e.g. its implementation will be sync)
        return new JmsChannelBindingDefinition();
    }

    /**
     * Generates intent metadata
     *
     * @param binding  the binding
     * @param metadata the JSM metadata
     */
    private void generateIntents(LogicalBinding<JmsBindingDefinition> binding, JmsBindingMetadata metadata) {
        LogicalChannel parent = (LogicalChannel) binding.getParent();
        if (binding.getDefinition().getIntents().contains(DURABLE_INTENT) || parent.getDefinition().getIntents().contains(DURABLE_INTENT)) {
            metadata.setDurable(true);
        }
        if (binding.getDefinition().getIntents().contains(NON_PERSISTENT_INTENT) || parent.getDefinition().getIntents().contains(NON_PERSISTENT_INTENT)) {
            metadata.getHeaders().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }

    }

    private boolean isJAXB(List<DataType> eventTypes) {
        for (DataType eventType : eventTypes) {
            if (JAXB.equals(eventType.getDatabinding())) {
                return true;
            }
        }
        return false;
    }

}