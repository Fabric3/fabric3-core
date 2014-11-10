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
package org.fabric3.api.binding.jms.builder;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 *
 */
public class JmsBindingDefinitionBuilder extends AbstractBuilder {
    private JmsBindingDefinition binding;

    public static JmsBindingDefinitionBuilder newBuilder() {
        return new JmsBindingDefinitionBuilder();
    }

    private JmsBindingDefinitionBuilder() {
        this("jms.binding");
    }

    private JmsBindingDefinitionBuilder(String name) {
        this.binding = new JmsBindingDefinition(name, new JmsBindingMetadata());
    }

    public JmsBindingDefinition build() {
        checkState();
        freeze();
        DestinationDefinition destinationDefinition = binding.getJmsMetadata().getDestination();
        if (destinationDefinition == null) {
            throw new IllegalArgumentException("Destination not defined for JMS binding");
        }
        String target = destinationDefinition.getName();
        URI bindingUri = URI.create("jms://" + target);
        binding.setGeneratedTargetUri(bindingUri);
        return binding;
    }

    public JmsBindingDefinitionBuilder policySet(QName policy) {
        checkState();
        binding.addPolicySet(policy);
        return this;
    }

    public JmsBindingDefinitionBuilder intent(QName intent) {
        checkState();
        binding.addIntent(intent);
        return this;
    }

    public JmsBindingDefinitionBuilder cacheLevel(CacheLevel level) {
        checkState();
        binding.getJmsMetadata().setCacheLevel(level);
        return this;
    }

    public JmsBindingDefinitionBuilder subscriptionId(String id) {
        checkState();
        binding.getJmsMetadata().setSubscriptionId(id);
        return this;
    }

    public JmsBindingDefinitionBuilder connectionFactoryName(String name) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().setName(name);
        return this;
    }

    public JmsBindingDefinitionBuilder connectionFactoryProperty(String name, String value) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().addProperty(name, value);
        return this;
    }

    public JmsBindingDefinitionBuilder connectionFactoryCreate(CreateOption option) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().setCreate(option);
        return this;
    }

    public JmsBindingDefinitionBuilder destination(String name, DestinationType type, CreateOption option) {
        checkState();
        DestinationDefinition definition = new DestinationDefinition();
        definition.setName(name);
        definition.setType(type);
        definition.setCreate(option);
        binding.getJmsMetadata().setDestination(definition);
        return this;
    }

    public JmsBindingDefinitionBuilder destination(String name, DestinationType type) {
        checkState();
        DestinationDefinition definition = new DestinationDefinition();
        definition.setName(name);
        definition.setType(type);
        binding.getJmsMetadata().setDestination(definition);
        return this;
    }

    public JmsBindingDefinitionBuilder correlationScheme(CorrelationScheme scheme) {
        checkState();
        binding.getJmsMetadata().setCorrelationScheme(scheme);
        return this;
    }

    public JmsBindingDefinitionBuilder durable(boolean durable) {
        checkState();
        binding.getJmsMetadata().setDurable(durable);
        return this;
    }

    public JmsBindingDefinitionBuilder idleLimit(int limit) {
        checkState();
        binding.getJmsMetadata().setIdleLimit(limit);
        return this;
    }

    public JmsBindingDefinitionBuilder jndiUrl(String url) {
        checkState();
        binding.getJmsMetadata().setJndiUrl(url);
        return this;
    }

    public JmsBindingDefinitionBuilder localDelivery(boolean delivery) {
        checkState();
        binding.getJmsMetadata().setLocalDelivery(delivery);
        return this;
    }

    public JmsBindingDefinitionBuilder maxMessages(int max) {
        checkState();
        binding.getJmsMetadata().setMaxMessagesToProcess(max);
        return this;
    }

    public JmsBindingDefinitionBuilder maxReceivers(int max) {
        checkState();
        binding.getJmsMetadata().setMaxReceivers(max);
        return this;
    }

    public JmsBindingDefinitionBuilder minReceivers(int min) {
        checkState();
        binding.getJmsMetadata().setMinReceivers(min);
        return this;
    }

    public JmsBindingDefinitionBuilder receiveTimeout(int timeout) {
        checkState();
        binding.getJmsMetadata().setReceiveTimeout(timeout);
        return this;
    }

    public JmsBindingDefinitionBuilder selector(String selector) {
        checkState();
        binding.getJmsMetadata().setMessageSelection(new MessageSelection(selector));
        return this;
    }

    public JmsBindingDefinitionBuilder recoveryInterval(long interval) {
        checkState();
        binding.getJmsMetadata().setRecoveryInterval(interval);
        return this;
    }

    public JmsBindingDefinitionBuilder response(ResponseDefinition definition) {
        checkState();
        binding.getJmsMetadata().setResponse(definition);
        return this;
    }

    public JmsBindingDefinitionBuilder responseTimeout(int timeout) {
        checkState();
        binding.getJmsMetadata().setResponseTimeout(timeout);
        return this;
    }

    public JmsBindingDefinitionBuilder uriSelector(String selector) {
        checkState();
        binding.getJmsMetadata().setUriMessageSelection(new MessageSelection(selector));
        return this;
    }

}
