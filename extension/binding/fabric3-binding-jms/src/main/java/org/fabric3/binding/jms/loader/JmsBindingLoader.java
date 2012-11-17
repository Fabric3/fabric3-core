/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.loader;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.common.ActivationSpec;
import org.fabric3.binding.jms.spi.common.CacheLevel;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.CorrelationScheme;
import org.fabric3.binding.jms.spi.common.CreateOption;
import org.fabric3.binding.jms.spi.common.DeliveryMode;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.DestinationType;
import org.fabric3.binding.jms.spi.common.HeadersDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.MessageSelection;
import org.fabric3.binding.jms.spi.common.OperationPropertiesDefinition;
import org.fabric3.binding.jms.spi.common.PropertyAwareObject;
import org.fabric3.binding.jms.spi.common.ResponseDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.model.type.binding.BindingHandlerDefinition;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;


/**
 * Loads a <code>&lt;binding.jms&gt;</code> entry in a composite.
 * <p/>
 * request/responseConnection are specified per the SCA JMS spec
 */
@EagerInit
public class JmsBindingLoader extends AbstractValidatingTypeLoader<JmsBindingDefinition> {
    private LoaderRegistry registry;
    private LoaderHelper loaderHelper;
    private int defaultResponseTimeout = 600000;  // set the default response wait to 10 minutes
    private int defaultTransactionTimeout = 30; // in seconds
    private int defaultReceiveTimeout = (defaultTransactionTimeout / 2) * 1000;  // set the timeout in milliseconds to half that of the trx timeout


    @Property(required = false)
    public void setDefaultResponseTimeout(int defaultResponseTimeout) {
        this.defaultResponseTimeout = defaultResponseTimeout;
    }

    @Property(required = false)
    public void setDefaultTransactionTimeout(int defaultTransactionTimeout) {
        this.defaultTransactionTimeout = defaultTransactionTimeout;
        defaultReceiveTimeout = (defaultTransactionTimeout / 2) * 1000;
    }

    public JmsBindingLoader(@Reference LoaderHelper loaderHelper, @Reference LoaderRegistry registry) {
        this.loaderHelper = loaderHelper;
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
                      "connectionFactory.template",
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
                      "min.receivers");
    }

    public JmsBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);

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
        JmsBindingDefinition definition = new JmsBindingDefinition(bindingName, metadata);
        NamespaceContext namespace = reader.getNamespaceContext();
        String targetNamespace = context.getTargetNamespace();

        parseCorrelationScheme(metadata, namespace, targetNamespace, reader, context);

        metadata.setJndiUrl(reader.getAttributeValue(null, "jndiURL"));
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        loadFabric3Attributes(metadata, reader, context);


        String name;
        while (true) {

            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                Location location = reader.getLocation();
                if ("handler".equals(name)) {
                    BindingHandlerDefinition handler = registry.load(reader, BindingHandlerDefinition.class, context);
                    definition.addHandler(handler);
                } else if ("destination".equals(name)) {
                    if (uri != null) {
                        InvalidJmsBinding error = new InvalidJmsBinding(
                                "A destination cannot be defined both as a JMS uri and as part of the binding.jms element",
                                location);
                        context.addError(error);
                    }
                    DestinationDefinition destination = loadDestination(reader, context);
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
                    DestinationDefinition destinationDefinition = metadata.getDestination();
                    if (destinationDefinition != null) {
                        String target = destinationDefinition.getName();
                        URI bindingUri = URI.create("jms://" + target);
                        definition.setGeneratedTargetUri(bindingUri);
                    } else if (metadata.getActivationSpec() != null) {
                        String target = metadata.getActivationSpec().getName();
                        URI bindingUri = URI.create("jms://" + target);
                        definition.setGeneratedTargetUri(bindingUri);
                    }
                    validate(definition, reader, context);
                    return definition;
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

        String templateName = reader.getAttributeValue(null, "connectionFactory.template");
        if (templateName != null) {
            metadata.getConnectionFactory().setTemplateName(templateName);
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
    }

    private ActivationSpec loadActivationSpec(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String jndiName = reader.getAttributeValue(null, "jndiName");
        CreateOption create = parseCreate(reader, context);
        ActivationSpec spec = new ActivationSpec(jndiName, create);
        loadProperties(reader, spec, "activationSpec");
        return spec;
    }


    private ResponseDefinition loadResponse(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location location = reader.getLocation();
        ResponseDefinition response = new ResponseDefinition();
        String name;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("destination".equals(name)) {
                    DestinationDefinition destination = loadDestination(reader, context);
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

    private DestinationDefinition loadDestination(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location location = reader.getLocation();
        DestinationDefinition destination = new DestinationDefinition();
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

    private void validate(JmsBindingDefinition definition, XMLStreamReader reader, IntrospectionContext context) {
        JmsBindingMetadata metadata = definition.getJmsMetadata();
        if (metadata.getConnectionFactory().isConfigured() && metadata.getDestination() == null) {
            Location location = reader.getLocation();
            InvalidJmsBinding error = new InvalidJmsBinding("A destination must be specified", location);
            context.addError(error);
        }
        if (metadata.getActivationSpec() != null && metadata.getConnectionFactory().isConfigured()) {
            Location location = reader.getLocation();
            InvalidJmsBinding error =
                    new InvalidJmsBinding("Activation spec and connection factory cannot both be specified on a JMS binding", location);
            context.addError(error);
        }
        DestinationDefinition requestDestination = metadata.getDestination();
        ActivationSpec requestSpec = metadata.getActivationSpec();
        if (requestDestination != null && requestSpec != null) {
            if (requestDestination.getName() != null && !requestDestination.getName().equals(requestSpec.getName())) {
                Location location = reader.getLocation();
                InvalidJmsBinding error =
                        new InvalidJmsBinding("Activation spec and destination configuration must refer to the same destination", location);
                context.addError(error);
            }
        }

        ResponseDefinition response = metadata.getResponse();
        if (response != null) {
            ActivationSpec responseSpec = response.getActivationSpec();
            if (responseSpec != null && response.getConnectionFactory().isConfigured()) {
                Location location = reader.getLocation();
                InvalidJmsBinding error =
                        new InvalidJmsBinding("Activation spec and connection factory cannot both be specified on a JMS binding", location);
                context.addError(error);
            }
            DestinationDefinition responseDestination = response.getDestination();
            if (responseDestination != null && responseSpec != null) {
                if (responseDestination.getName() != null && !responseDestination.getName().equals(responseSpec.getName())) {
                    Location location = reader.getLocation();
                    InvalidJmsBinding error =
                            new InvalidJmsBinding("Activation spec and destination configuration must refer to the same destination", location);
                    context.addError(error);
                }
            }
        }

        // validate operation properties
        Set<String> seen = new HashSet<String>();
        for (OperationPropertiesDefinition entry : metadata.getOperationProperties().values()) {
            String name = entry.getSelectedOperation();
            if (seen.contains(name)) {
                Location location = reader.getLocation();
                InvalidJmsBinding error = new InvalidJmsBinding("Duplicate selected operation for property defined: " + name, location);
                context.addError(error);
            } else {
                seen.add(name);
            }
        }
    }


}
