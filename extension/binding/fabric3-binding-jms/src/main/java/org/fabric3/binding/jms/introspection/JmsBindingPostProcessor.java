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
package org.fabric3.binding.jms.introspection;

import java.lang.reflect.AnnotatedElement;
import java.net.URI;

import org.fabric3.api.binding.jms.annotation.JMS;
import org.fabric3.api.binding.jms.annotation.JMSConfiguration;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects JMS binding information in a component implementation.
 */
@EagerInit
public class JmsBindingPostProcessor extends AbstractBindingPostProcessor<JMS> {

    public JmsBindingPostProcessor() {
        super(JMS.class);
    }

    protected Binding processReference(JMS annotation, Reference reference, Class<?> implClass, IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);
    }

    protected Binding processService(JMS annotation,
                                     Service<ComponentType> service,
                                     InjectingComponentType componentType,
                                     Class<?> implClass,
                                     IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);

    }

    protected Binding processServiceCallback(JMS annotation,
                                             Service<ComponentType> service,
                                             InjectingComponentType componentType,
                                             Class<?> implClass,
                                             IntrospectionContext context) {
        return createDefinition(annotation, annotation.callback(), implClass, context);
    }

    protected Binding processReferenceCallback(JMS annotation, Reference reference, Class<?> implClass, IntrospectionContext context) {
        return createDefinition(annotation, annotation.callback(), implClass, context);
    }

    private JmsBinding createDefinition(JMS annotation, JMSConfiguration configuration, Class<?> implClass, IntrospectionContext context) {
        if(!isActiveForEnvironment(annotation.environments())) {
            return null;
        }
        String name = annotation.name();
        if (name.isEmpty()) {
            name = "JMSBinding";
        }
        JmsBindingMetadata metadata = new JmsBindingMetadata();

        parseConfiguration(configuration, metadata, implClass, context);

        JmsBinding binding = new JmsBinding(name, metadata);

        // needed for callbacks
        Destination destination = metadata.getDestination();
        if (destination != null) {
            String target = destination.getName();
            URI bindingUri = URI.create("jms://" + target);
            binding.setGeneratedTargetUri(bindingUri);
        }
        return binding;
    }

    private void parseConfiguration(JMSConfiguration configuration, JmsBindingMetadata metadata, Class<?> implClass, IntrospectionContext context) {
        ConnectionFactoryDefinition factory = new ConnectionFactoryDefinition();
        factory.setName(getNullableValue(configuration.connectionFactory()));
        metadata.setConnectionFactory(factory);

        Destination destination = new Destination();
        destination.setName(configuration.destination());
        metadata.setDestination(destination);

        metadata.setCacheLevel(configuration.cacheLevel());
        metadata.setSubscriptionId(getNullableValue(configuration.subscriptionId()));
        metadata.setCorrelationScheme(configuration.correlation());
        metadata.setDurable(configuration.durable());
        metadata.setIdleLimit(configuration.idleLimit());
        metadata.setLocalDelivery(configuration.localDelivery());
        metadata.setMaxMessagesToProcess(configuration.maxMessagesToProcess());
        metadata.setMaxReceivers(configuration.maxReceivers());
        metadata.setMessageSelection(new MessageSelection(configuration.selector()));
        metadata.setMinReceivers(configuration.minReceivers());
        metadata.setReceiveTimeout(configuration.receiveTimeout());
        metadata.setResponseTimeout(configuration.responseTimeout());
        metadata.setRecoveryInterval(configuration.recoveryInterval());
        metadata.setClientAcknowledge(configuration.clientAcknowledge());
        metadata.setBackoffPeriod(configuration.backoffPeriod());

        parseResponse(configuration, metadata, implClass, implClass, context);
    }

    private void parseResponse(JMSConfiguration configuration,
                               JmsBindingMetadata metadata,
                               AnnotatedElement element,
                               Class<?> implClass,
                               IntrospectionContext context) {
        if (configuration.responseConnectionFactory().isEmpty() && configuration.responseDestination().isEmpty()) {
            return;
        }

        if (!configuration.responseConnectionFactory().isEmpty() && configuration.responseDestination().isEmpty()) {
            InvalidAnnotation error = new InvalidAnnotation("A response destination was not specified", element, configuration, implClass);
            context.addError(error);
        }

        ResponseDefinition response = new ResponseDefinition();
        Destination responseDestination = new Destination();
        responseDestination.setName(configuration.responseDestination());
        response.setDestination(responseDestination);

        ConnectionFactoryDefinition responseFactory = new ConnectionFactoryDefinition();
        responseFactory.setName(configuration.responseConnectionFactory());
        response.setConnectionFactory(responseFactory);
        metadata.setResponse(response);
    }

}
