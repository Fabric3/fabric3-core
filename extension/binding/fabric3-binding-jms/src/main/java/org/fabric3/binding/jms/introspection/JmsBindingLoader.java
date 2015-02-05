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
package org.fabric3.binding.jms.introspection;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.jms.model.ActivationSpec;
import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.HeadersDefinition;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.OperationPropertiesDefinition;
import org.fabric3.api.binding.jms.model.PropertyAwareObject;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Loads a <code>&lt;binding.jms&gt;</code> entry in a composite.  request/responseConnection are specified per the SCA JMS spec
 */
@EagerInit
@Key(Constants.SCA_PREFIX + "binding.jms")
public class JmsBindingLoader extends AbstractValidatingTypeLoader<JmsBinding> {
    private LoaderRegistry registry;
    private int defaultResponseTimeout = 600000;  // set the default response wait to 10 minutes
    private int defaultTransactionTimeout = 30; // in seconds
    private int defaultReceiveTimeout = (defaultTransactionTimeout / 2) * 1000;  // set the timeout in milliseconds to half that of the trx timeout

    @Property(required = false)
    @Source("$systemConfig//f3:jms/@response.timeout")
    public void setDefaultResponseTimeout(int defaultResponseTimeout) {
        this.defaultResponseTimeout = defaultResponseTimeout;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/@transaction.timeout")
    public void setDefaultTransactionTimeout(int defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
        defaultReceiveTimeout = (defaultTransactionTimeout / 2) * 1000;
    }

    public JmsBindingLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
        addAttributes("uri",
                      "activationSpec",
                      "wireFormat",
                      "jndiURL",
                      "initialContextFactory",
                      "requires",
                      "messageSelection",
                      "policySets",
                      "create",
                      "type",
                      "destination",
                      "connectionFactory",
                      "messageSelection",
                      "type",
                      "timeToLive",
                      "resourceAdapter",
                      "priority",
                      "deliveryMode",
                      "correlationScheme",
                      "name",
                      "cache",
                      "idle.limit",
                      "receive.timeout",
                      "response.timeout",
                      "max.messages",
                      "recovery.interval",
                      "max.receivers",
                      "min.receivers",
                      "clientAcknowledge");
    }

    public JmsBinding load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String bindingName = reader.getAttributeValue(null, "name");

        JmsBindingMetadata metadata;
        String uri = reader.getAttributeValue(null, "uri");
        if (uri != null) {
            try {
                metadata = JmsLoaderHelper.parseUri(uri);
            } catch (JmsUriException e) {
                InvalidValue failure = new InvalidValue("Invalid JMS binding URI: " + uri, startLocation, e);
                context.addError(failure);
                return null;
            }
        } else {
            metadata = new JmsBindingMetadata();
        }
        JmsBinding binding = new JmsBinding(bindingName, metadata);

        NamespaceContext namespace = reader.getNamespaceContext();
        String targetNamespace = context.getTargetNamespace();

        parseCorrelationScheme(metadata, namespace, targetNamespace, reader, context);

        metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));

        loadFabric3Attributes(metadata, reader, context);

        validateAttributes(reader, context, binding);

        String name;
        while (true) {

            switch (reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    Location location = reader.getLocation();
                    if ("handler".equals(name)) {
                        BindingHandler handler = registry.load(reader, BindingHandler.class, context);
                        binding.addHandler(handler);
                    } else if ("destination".equals(name)) {
                        if (uri != null) {
                            InvalidJmsBinding error = new InvalidJmsBinding(
                                    "A destination cannot be defined both as a JMS uri and as part of the binding.jms element",
                                    location,
                                    binding);
                            context.addError(error);
                        }
                        Destination destination = loadDestination(reader, context);
                        metadata.setDestination(destination);
                    } else if ("connectionFactory".equals(name)) {
                        ConnectionFactoryDefinition connectionFactory = loadConnectionFactory(reader, context);
                        metadata.setConnectionFactory(connectionFactory);
                    } else if ("activationSpec".equals(name)) {
                        ActivationSpec spec = loadActivationSpec(reader, context);
                        metadata.setActivationSpec(spec);
                    } else if ("response".equals(name)) {
                        ResponseDefinition response = loadResponse(reader, context);
                        metadata.setResponse(response);
                    } else if ("headers".equals(name)) {
                        HeadersDefinition headers = metadata.getHeaders();
                        loadHeaders(headers, reader, context);
                    } else if ("messageSelection".equals(name)) {
                        MessageSelection messageSelection = loadMessageSelection(reader, context);
                        metadata.setMessageSelection(messageSelection);
                    } else if ("operationProperties".equals(name)) {
                        OperationPropertiesDefinition operationProperties = loadOperationProperties(reader, context);
                        metadata.addOperationProperties(operationProperties.getName(), operationProperties);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("binding.jms".equals(name)) {
                        // needed for callbacks
                        Destination destination = metadata.getDestination();
                        if (destination != null) {
                            String target = destination.getName();
                            URI bindingUri = URI.create("jms://" + target);
                            binding.setGeneratedTargetUri(bindingUri);
                        } else if (metadata.getActivationSpec() != null) {
                            String target = metadata.getActivationSpec().getName();
                            URI bindingUri = URI.create("jms://" + target);
                            binding.setGeneratedTargetUri(bindingUri);
                        }
                        validate(binding, reader, context);
                        return binding;
                    }
                    break;
            }

        }
    }

    private void parseCorrelationScheme(JmsBindingMetadata metadata,
                                        NamespaceContext namespace,
                                        String targetNamespace,
                                        XMLStreamReader reader,
                                        IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String correlationScheme = reader.getAttributeValue(null, "correlationScheme");
        if (correlationScheme != null) {
            QName scheme = LoaderUtil.getQName(correlationScheme, targetNamespace, namespace);
            // support lax namespaces
            String localPart = scheme.getLocalPart();
            if ("messageID".equalsIgnoreCase(localPart)) {
                metadata.setCorrelationScheme(CorrelationScheme.MESSAGE_ID);
            } else if ("correlationID".equalsIgnoreCase(localPart)) {
                metadata.setCorrelationScheme(CorrelationScheme.CORRELATION_ID);
            } else if ("none".equalsIgnoreCase(localPart)) {
                metadata.setCorrelationScheme(CorrelationScheme.NONE);
            } else {
                InvalidValue error = new InvalidValue("Invalid value specified for correlationScheme attribute: " + localPart, startLocation);
                context.addError(error);
            }
        }
    }

    private void loadFabric3Attributes(JmsBindingMetadata metadata, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String cacheLevel = reader.getAttributeValue(null, "cache");
        if ("connection".equalsIgnoreCase(cacheLevel)) {
            metadata.setCacheLevel((CacheLevel.CONNECTION));
        } else if ("session".equalsIgnoreCase(cacheLevel)) {
            metadata.setCacheLevel((CacheLevel.ADMINISTERED_OBJECTS));
        } else if (cacheLevel != null) {
            InvalidValue error = new InvalidValue("Invalid cache level attribute", startLocation);
            context.addError(error);
        }

        String idleLimit = reader.getAttributeValue(null, "idle.limit");
        if (idleLimit != null) {
            try {
                int val = Integer.parseInt(idleLimit);
                metadata.setIdleLimit(val);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid idle.limit attribute", startLocation, e);
                context.addError(error);
            }
        }

        String receiveTimeout = reader.getAttributeValue(null, "receive.timeout");
        int receiveVal = defaultReceiveTimeout;
        if (receiveTimeout != null) {
            try {
                receiveVal = Integer.parseInt(receiveTimeout);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid receive.timeout attribute", startLocation, e);
                context.addError(error);
            }
        }
        metadata.setReceiveTimeout(receiveVal);

        String responseTimeout = reader.getAttributeValue(null, "response.timeout");
        int responseVal = defaultResponseTimeout;
        if (responseTimeout != null) {
            try {
                responseVal = Integer.parseInt(responseTimeout);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid response.timeout attribute", startLocation, e);
                context.addError(error);
            }
        }
        metadata.setResponseTimeout(responseVal);

        String maxMessagesProcess = reader.getAttributeValue(null, "max.messages");
        if (maxMessagesProcess != null) {
            try {
                int val = Integer.parseInt(maxMessagesProcess);
                metadata.setMaxMessagesToProcess(val);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid max.messages attribute", startLocation, e);
                context.addError(error);
            }
        }

        String recoveryInterval = reader.getAttributeValue(null, "recovery.interval");
        if (recoveryInterval != null) {
            try {
                int val = Integer.parseInt(recoveryInterval);
                metadata.setRecoveryInterval(val);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid recovery.interval attribute", startLocation, e);
                context.addError(error);
            }
        }

        String max = reader.getAttributeValue(null, "max.receivers");
        if (max != null) {
            try {
                int val = Integer.parseInt(max);
                metadata.setMaxReceivers(val);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid max.receivers attribute", startLocation, e);
                context.addError(error);
            }
        }
        String min = reader.getAttributeValue(null, "min.receivers");
        if (min != null) {
            try {
                int val = Integer.parseInt(min);
                metadata.setMinReceivers(val);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid min.receivers attribute", startLocation, e);
                context.addError(error);
            }
        }

        String ack = reader.getAttributeValue(null, "clientAcknowledge");
        metadata.setClientAcknowledge(Boolean.valueOf(ack));
    }

    private ActivationSpec loadActivationSpec(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String jndiName = reader.getAttributeValue(null, "jndiName");
        CreateOption create = parseCreate(reader, context);
        ActivationSpec spec = new ActivationSpec(jndiName, create);
        loadProperties(reader, spec, "activationSpec");
        return spec;
    }

    private ResponseDefinition loadResponse(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        ResponseDefinition response = new ResponseDefinition();
        String name;
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("destination".equals(name)) {
                        Destination destination = loadDestination(reader, context);
                        response.setDestination(destination);
                    } else if ("activationSpec".equals(name)) {
                        ActivationSpec spec = loadActivationSpec(reader, context);
                        response.setActivationSpec(spec);
                    } else if ("connectionFactory".equals(name)) {
                        ConnectionFactoryDefinition connectionFactory = loadConnectionFactory(reader, context);
                        response.setConnectionFactory(connectionFactory);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("response".equals(name)) {
                        return response;
                    }
                    break;
            }
        }
    }

    private ConnectionFactoryDefinition loadConnectionFactory(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        ConnectionFactoryDefinition connectionFactory = new ConnectionFactoryDefinition();
        connectionFactory.setName(reader.getAttributeValue(null, "jndiName"));
        CreateOption create = parseCreate(reader, context);
        connectionFactory.setCreate(create);
        loadProperties(reader, connectionFactory, "connectionFactory");
        return connectionFactory;
    }

    private Destination loadDestination(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location location = reader.getLocation();
        Destination destination = new Destination();
        String jndiName = reader.getAttributeValue(null, "jndiName");
        if (jndiName != null) {
            destination.setName(jndiName);
        } else {
            // support name attribute as well
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                MissingAttribute error = new MissingAttribute("Destination must have either a jndiName or name attribute set", location);
                context.addError(error);
            }
        }
        CreateOption create = parseCreate(reader, context);
        destination.setCreate(create);
        String type = reader.getAttributeValue(null, "type");
        if (type != null) {
            if ("queue".equalsIgnoreCase(type)) {
                destination.setType(DestinationType.QUEUE);
            } else if ("topic".equalsIgnoreCase(type)) {
                destination.setType(DestinationType.TOPIC);
            } else {
                InvalidValue error = new InvalidValue("Invalid value specified for destination type: " + type, location);
                context.addError(error);
            }
        }
        loadProperties(reader, destination, "destination");
        return destination;
    }

    private CreateOption parseCreate(XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String create = reader.getAttributeValue(null, "create");
        if (create != null) {
            if ("always".equals(create)) {
                return CreateOption.ALWAYS;
            } else if ("never".equalsIgnoreCase(create)) {
                return CreateOption.NEVER;
            } else if ("ifNotExist".equalsIgnoreCase(create)) {
                return CreateOption.IF_NOT_EXIST;
            } else {
                InvalidValue error = new InvalidValue("Invalid value specified for create attribute: " + create, startLocation);
                context.addError(error);
            }
        }
        return CreateOption.IF_NOT_EXIST;
    }

    private void loadHeaders(HeadersDefinition headers, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String deliveryMode = reader.getAttributeValue(null, "deliveryMode");
        if (deliveryMode != null) {
            if ("PERSISTENT".equalsIgnoreCase(deliveryMode)) {
                headers.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else if ("NONPERSISTENT".equalsIgnoreCase(deliveryMode)) {
                headers.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            } else {
                InvalidValue failure = new InvalidValue("Invalid delivery mode: " + deliveryMode, startLocation);
                context.addError(failure);
            }
        }
        String priority = reader.getAttributeValue(null, "priority");
        if (priority != null) {
            try {
                Integer value = Integer.valueOf(priority);
                headers.setPriority(value);
                if (value < 0 || value > 9) {
                    InvalidValue failure = new InvalidValue("Invalid priority: " + priority + ". Values must be from 0-9.", startLocation);
                    context.addError(failure);
                }
            } catch (NumberFormatException nfe) {
                InvalidValue failure = new InvalidValue("Invalid priority: " + priority, startLocation, nfe);
                context.addError(failure);
            }
        }
        String timeToLive = reader.getAttributeValue(null, "timeToLive");
        if (timeToLive != null) {
            try {
                headers.setTimeToLive(Long.valueOf(timeToLive));
            } catch (NumberFormatException nfe) {
                InvalidValue failure = new InvalidValue("Invalid time-to-live value: " + timeToLive, startLocation, nfe);
                context.addError(failure);
            }
        }
        headers.setJmsType(reader.getAttributeValue(null, "type"));
        loadProperties(reader, headers, "headers");
    }

    private MessageSelection loadMessageSelection(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location location = reader.getLocation();
        String selector = reader.getAttributeValue(null, "selector");
        if (selector == null) {
            MissingAttribute error = new MissingAttribute("Selector not specified for message selection", location);
            context.addError(error);
            selector = "invalid";
        }
        MessageSelection messageSelection = new MessageSelection(selector);
        loadProperties(reader, messageSelection, "messageSelection");
        return messageSelection;
    }

    private OperationPropertiesDefinition loadOperationProperties(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        OperationPropertiesDefinition operationProperties = new OperationPropertiesDefinition();
        operationProperties.setName(reader.getAttributeValue(null, "name"));
        operationProperties.setSelectedOperation(reader.getAttributeValue(null, "selectedOperation"));
        String name;
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("headers".equals(name)) {
                        HeadersDefinition headers = operationProperties.getHeaders();
                        loadHeaders(headers, reader, context);
                    } else if ("property".equals(name)) {
                        loadProperty(reader, operationProperties);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("operationProperties".equals(name)) {
                        return operationProperties;
                    }
                    break;
            }
        }

    }

    private void loadProperties(XMLStreamReader reader, PropertyAwareObject parent, String parentName) throws XMLStreamException {
        String name;
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("property".equals(name)) {
                        loadProperty(reader, parent);
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if (parentName.equals(name)) {
                        return;
                    }
                    break;
            }
        }
    }

    private void loadProperty(XMLStreamReader reader, PropertyAwareObject parent) throws XMLStreamException {
        String key = reader.getAttributeValue(null, "name");
        String value = reader.getElementText();
        parent.addProperty(key, value);
    }

    private void validate(JmsBinding definition, XMLStreamReader reader, IntrospectionContext context) {
        JmsBindingMetadata metadata = definition.getJmsMetadata();
        if (metadata.getConnectionFactory().isConfigured() && metadata.getDestination() == null) {
            Location location = reader.getLocation();
            InvalidJmsBinding error = new InvalidJmsBinding("A destination must be specified", location, definition);
            context.addError(error);
        }
        if (metadata.getActivationSpec() != null && metadata.getConnectionFactory().isConfigured()) {
            Location location = reader.getLocation();
            InvalidJmsBinding error = new InvalidJmsBinding("Activation spec and connection factory cannot both be specified on a JMS binding",
                                                            location,
                                                            definition);
            context.addError(error);
        }
        Destination requestDestination = metadata.getDestination();
        ActivationSpec requestSpec = metadata.getActivationSpec();
        if (requestDestination != null && requestSpec != null) {
            if (requestDestination.getName() != null && !requestDestination.getName().equals(requestSpec.getName())) {
                Location location = reader.getLocation();
                InvalidJmsBinding error = new InvalidJmsBinding("Activation spec and destination configuration must refer to the same destination",
                                                                location,
                                                                definition);
                context.addError(error);
            }
        }

        ResponseDefinition response = metadata.getResponse();
        if (response != null) {
            ActivationSpec responseSpec = response.getActivationSpec();
            if (responseSpec != null && response.getConnectionFactory().isConfigured()) {
                Location location = reader.getLocation();
                InvalidJmsBinding error = new InvalidJmsBinding("Activation spec and connection factory cannot both be specified on a JMS binding",
                                                                location,
                                                                definition);
                context.addError(error);
            }
            Destination responseDestination = response.getDestination();
            if (responseDestination != null && responseSpec != null) {
                if (responseDestination.getName() != null && !responseDestination.getName().equals(responseSpec.getName())) {
                    Location location = reader.getLocation();
                    InvalidJmsBinding error = new InvalidJmsBinding("Activation spec and destination configuration must refer to the same destination",
                                                                    location,
                                                                    definition);
                    context.addError(error);
                }
            }
        }

        // validate operation properties
        Set<String> seen = new HashSet<>();
        for (OperationPropertiesDefinition entry : metadata.getOperationProperties().values()) {
            String name = entry.getSelectedOperation();
            if (seen.contains(name)) {
                Location location = reader.getLocation();
                InvalidJmsBinding error = new InvalidJmsBinding("Duplicate selected operation for property defined: " + name, location, definition);
                context.addError(error);
            } else {
                seen.add(name);
            }
        }
    }

}
