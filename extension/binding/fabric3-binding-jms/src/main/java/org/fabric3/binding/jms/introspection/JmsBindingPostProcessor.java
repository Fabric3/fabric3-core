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
package org.fabric3.binding.jms.introspection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.net.URI;

import org.fabric3.api.binding.jms.annotation.JMS;
import org.fabric3.api.binding.jms.annotation.JMSConfiguration;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ReferenceDefinition;
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

    protected BindingDefinition processService(JMS annotation,
                                               AbstractService<?> service,
                                               InjectingComponentType componentType,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);

    }

    protected BindingDefinition processServiceCallback(JMS annotation,
                                                       AbstractService<?> service,
                                                       InjectingComponentType componentType,
                                                       Class<?> implClass,
                                                       IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);
    }

    protected BindingDefinition processReference(JMS annotation,
                                                 ReferenceDefinition reference,
                                                 AccessibleObject object,
                                                 Class<?> implClass,
                                                 IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);
    }

    protected BindingDefinition processReferenceCallback(JMS annotation,
                                                         ReferenceDefinition reference,
                                                         AccessibleObject object,
                                                         Class<?> implClass,
                                                         IntrospectionContext context) {
        return createDefinition(annotation, annotation.value(), implClass, context);
    }

    private JmsBindingDefinition createDefinition(JMS annotation, JMSConfiguration configuration, Class<?> implClass, IntrospectionContext context) {
        String name = annotation.name();
        if (name.isEmpty()) {
            name = "JMSBinding";
        }
        JmsBindingMetadata metadata = new JmsBindingMetadata();

        parseConfiguration(configuration, metadata, implClass, context);

        JmsBindingDefinition definition = new JmsBindingDefinition(name, metadata);

        // needed for callbacks
        DestinationDefinition destinationDefinition = metadata.getDestination();
        if (destinationDefinition != null) {
            String target = destinationDefinition.getName();
            URI bindingUri = URI.create("jms://" + target);
            definition.setGeneratedTargetUri(bindingUri);
        }
        return definition;
    }

    private void parseConfiguration(JMSConfiguration configuration, JmsBindingMetadata metadata, Class<?> implClass, IntrospectionContext context) {
        ConnectionFactoryDefinition factory = new ConnectionFactoryDefinition();
        factory.setName(getNullibleValue(configuration.connectionFactory()));
        metadata.setConnectionFactory(factory);

        DestinationDefinition destination = new DestinationDefinition();
        destination.setName(configuration.destination());
        metadata.setDestination(destination);

        metadata.setCacheLevel(configuration.cacheLevel());
        metadata.setClientIdSpecifier(getNullibleValue(configuration.clientIdSpecifier()));
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
        DestinationDefinition responseDestination = new DestinationDefinition();
        responseDestination.setName(configuration.responseDestination());
        response.setDestination(responseDestination);

        ConnectionFactoryDefinition responseFactory = new ConnectionFactoryDefinition();
        responseFactory.setName(configuration.responseConnectionFactory());
        response.setConnectionFactory(responseFactory);
        metadata.setResponse(response);
    }

}
