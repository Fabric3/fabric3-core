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

import java.net.URI;

import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 *
 */
public class JmsBindingBuilder extends AbstractBuilder {
    private JmsBinding binding;

    public static JmsBindingBuilder newBuilder() {
        return new JmsBindingBuilder();
    }

    private JmsBindingBuilder() {
        this("jms.binding");
    }

    private JmsBindingBuilder(String name) {
        this.binding = new JmsBinding(name, new JmsBindingMetadata());
    }

    public JmsBinding build() {
        checkState();
        freeze();
        Destination destination = binding.getJmsMetadata().getDestination();
        if (destination == null) {
            throw new IllegalArgumentException("Destination not defined for JMS binding");
        }
        String target = destination.getName();
        URI bindingUri = URI.create("jms://" + target);
        binding.setGeneratedTargetUri(bindingUri);
        return binding;
    }

    public JmsBindingBuilder cacheLevel(CacheLevel level) {
        checkState();
        binding.getJmsMetadata().setCacheLevel(level);
        return this;
    }

    public JmsBindingBuilder subscriptionId(String id) {
        checkState();
        binding.getJmsMetadata().setSubscriptionId(id);
        return this;
    }

    public JmsBindingBuilder connectionFactoryName(String name) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().setName(name);
        return this;
    }

    public JmsBindingBuilder connectionFactoryProperty(String name, String value) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().addProperty(name, value);
        return this;
    }

    public JmsBindingBuilder connectionFactoryCreate(CreateOption option) {
        checkState();
        binding.getJmsMetadata().getConnectionFactory().setCreate(option);
        return this;
    }

    public JmsBindingBuilder destination(String name, DestinationType type, CreateOption option) {
        checkState();
        Destination definition = new Destination();
        definition.setName(name);
        definition.setType(type);
        definition.setCreate(option);
        binding.getJmsMetadata().setDestination(definition);
        return this;
    }

    public JmsBindingBuilder destination(String name, DestinationType type) {
        checkState();
        Destination definition = new Destination();
        definition.setName(name);
        definition.setType(type);
        binding.getJmsMetadata().setDestination(definition);
        return this;
    }

    public JmsBindingBuilder correlationScheme(CorrelationScheme scheme) {
        checkState();
        binding.getJmsMetadata().setCorrelationScheme(scheme);
        return this;
    }

    public JmsBindingBuilder durable(boolean durable) {
        checkState();
        binding.getJmsMetadata().setDurable(durable);
        return this;
    }

    public JmsBindingBuilder idleLimit(int limit) {
        checkState();
        binding.getJmsMetadata().setIdleLimit(limit);
        return this;
    }

    public JmsBindingBuilder jndiUrl(String url) {
        checkState();
        binding.getJmsMetadata().setJndiUrl(url);
        return this;
    }

    public JmsBindingBuilder localDelivery(boolean delivery) {
        checkState();
        binding.getJmsMetadata().setLocalDelivery(delivery);
        return this;
    }

    public JmsBindingBuilder maxMessages(int max) {
        checkState();
        binding.getJmsMetadata().setMaxMessagesToProcess(max);
        return this;
    }

    public JmsBindingBuilder maxReceivers(int max) {
        checkState();
        binding.getJmsMetadata().setMaxReceivers(max);
        return this;
    }

    public JmsBindingBuilder minReceivers(int min) {
        checkState();
        binding.getJmsMetadata().setMinReceivers(min);
        return this;
    }

    public JmsBindingBuilder clientAcknowledge(boolean ack) {
        checkState();
        binding.getJmsMetadata().setClientAcknowledge(ack);
        return this;
    }

    public JmsBindingBuilder receiveTimeout(int timeout) {
        checkState();
        binding.getJmsMetadata().setReceiveTimeout(timeout);
        return this;
    }

    public JmsBindingBuilder selector(String selector) {
        checkState();
        binding.getJmsMetadata().setMessageSelection(new MessageSelection(selector));
        return this;
    }

    public JmsBindingBuilder recoveryInterval(long interval) {
        checkState();
        binding.getJmsMetadata().setRecoveryInterval(interval);
        return this;
    }

    public JmsBindingBuilder response(ResponseDefinition definition) {
        checkState();
        binding.getJmsMetadata().setResponse(definition);
        return this;
    }

    public JmsBindingBuilder responseTimeout(int timeout) {
        checkState();
        binding.getJmsMetadata().setResponseTimeout(timeout);
        return this;
    }

    public JmsBindingBuilder uriSelector(String selector) {
        checkState();
        binding.getJmsMetadata().setUriMessageSelection(new MessageSelection(selector));
        return this;
    }

}
