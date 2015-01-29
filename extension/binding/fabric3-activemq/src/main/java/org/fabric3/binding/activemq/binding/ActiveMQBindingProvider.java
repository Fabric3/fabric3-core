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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.activemq.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.binding.BindingMatchResult;
import org.fabric3.spi.domain.generator.binding.BindingProvider;
import org.fabric3.spi.domain.generator.binding.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;

/**
 * Implements binding.sca using ActiveMQ. <p/> <p/> By default, this provider uses an embedded broker, which forwards messages to peer brokers in a zone. To
 * configure the provider to use a remote broker, the <code>brokerUrl</code> property may be set to the appropriate broker location. <p/> <p/> Also, the
 * provider uses default connection factory configurations; to use specific connection factories, set the <code>connectionFactory</code> and
 * <code>xaConnectionFactory</code> properties.
 */
@EagerInit
public class ActiveMQBindingProvider implements BindingProvider {
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, JmsBindingDefinition.BINDING_QNAME);
    public static final String MANAGED_TRANSACTION = "managedTransaction";
    public static final String MANAGED_TRANSACTION_GLOBAL = "managedTransaction.global";
    public static final String MANAGED_TRANSACTION_LOCAL = "managedTransaction.local";

    private String connectionFactory;
    private String xaConnectionFactory;
    private boolean enabled = true;
    private CacheLevel level = CacheLevel.ADMINISTERED_OBJECTS;

    private ProviderMonitor monitor;

    @Property(required = false)
    public void setConnectionFactory(String name) {
        this.connectionFactory = name;
    }

    @Property(required = false)
    public void setXaConnectionFactory(String name) {
        this.xaConnectionFactory = name;
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Property(required = false)
    public void setLevel(String cacheLevel) {
        if ("connection".equalsIgnoreCase(cacheLevel)) {
            level = CacheLevel.CONNECTION;
        } else if ("session".equalsIgnoreCase(cacheLevel)) {
            level = CacheLevel.ADMINISTERED_OBJECTS;
        } else if ("none".equalsIgnoreCase(cacheLevel)) {
            level = CacheLevel.NONE;
        } else {
            monitor.error("Invalid JMS provider cache level value: " + cacheLevel + ". Ignoring value.");
        }
    }

    public ActiveMQBindingProvider(@Monitor ProviderMonitor monitor) {
        this.monitor = monitor;
    }

    public QName getType() {
        return JmsBindingDefinition.BINDING_QNAME;
    }

    public BindingMatchResult canBind(LogicalWire wire) {
        if (!enabled) {
            return NO_MATCH;
        }
        return new BindingMatchResult(true, getType());
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        if (!enabled) {
            return NO_MATCH;
        }
        return new BindingMatchResult(true, getType());
    }

    public BindingMatchResult canBind(LogicalService service) {
        if (!enabled) {
            return NO_MATCH;
        }
        return new BindingMatchResult(true, getType());
    }

    public void bind(LogicalWire wire) throws BindingSelectionException {
        LogicalReference source = wire.getSource().getLeafReference();
        LogicalService target = wire.getTarget().getLeafService();
        QName deployable = source.getParent().getDeployable();

        ServiceContract targetContract = target.getDefinition().getServiceContract();
        // determine if the contract is request-response
        boolean response = isRequestResponse(targetContract);

        // setup forward bindings
        // derive the forward queue name from the service name
        String forwardQueue = target.getUri().toString();
        JmsBindingDefinition referenceDefinition = createBindingDefinition(forwardQueue, response, false);  // XA not enabled on references
        LogicalBinding<JmsBindingDefinition> referenceBinding = new LogicalBinding<>(referenceDefinition, source, deployable);
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);

        boolean bindTarget = bindTarget(target);

        if (bindTarget) {
            boolean xa = isXA(target);
            JmsBindingDefinition serviceDefinition = createBindingDefinition(forwardQueue, response, xa);
            LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<>(serviceDefinition, target, deployable);
            serviceBinding.setAssigned(true);
            target.addBinding(serviceBinding);
        }

        // check if the interface is bidirectional
        if (targetContract.getCallbackContract() != null) {
            // setup callback bindings
            // derive the callback queue name from the reference name since multiple clients can connect to a service
            String callbackQueue = target.getUri().toString() + "Callback";
            boolean callbackXa = isXA(target);

            JmsBindingDefinition callbackReferenceDefinition = createBindingDefinition(callbackQueue, false, callbackXa);
            LogicalBinding<JmsBindingDefinition> callbackReferenceBinding = new LogicalBinding<>(callbackReferenceDefinition, source, deployable);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);
            callbackReferenceDefinition.setGeneratedTargetUri(createCallbackUri(target));

            if (bindTarget) {
                JmsBindingDefinition callbackServiceDefinition = createBindingDefinition(callbackQueue,
                                                                                         false,
                                                                                         false); // XA not enabled on service side callback
                LogicalBinding<JmsBindingDefinition> callbackServiceBinding = new LogicalBinding<>(callbackServiceDefinition, target, deployable);
                callbackServiceBinding.setAssigned(true);
                target.addCallbackBinding(callbackServiceBinding);
                callbackServiceDefinition.setGeneratedTargetUri(createCallbackUri(target));
            }
        }
    }

    public void bind(LogicalService service) throws BindingSelectionException {
        String forwardQueue = service.getUri().toString();
        QName deployable = service.getParent().getDeployable();
        ServiceContract targetContract = service.getDefinition().getServiceContract();
        boolean response = isRequestResponse(targetContract);

        boolean xa = isXA(service);
        JmsBindingDefinition serviceDefinition = createBindingDefinition(forwardQueue, response, xa);
        LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<>(serviceDefinition, service, deployable);
        serviceBinding.setAssigned(true);
        service.addBinding(serviceBinding);

        // check if the interface is bidirectional
        if (targetContract.getCallbackContract() != null) {
            String callbackQueue = service.getUri().toString() + "Callback";
            JmsBindingDefinition callbackServiceDefinition = createBindingDefinition(callbackQueue, false, false); // XA not enabled on service side callback
            LogicalBinding<JmsBindingDefinition> callbackServiceBinding = new LogicalBinding<>(callbackServiceDefinition, service, deployable);
            callbackServiceBinding.setAssigned(true);
            service.addCallbackBinding(callbackServiceBinding);
            callbackServiceDefinition.setGeneratedTargetUri(createCallbackUri(service));
        }
    }

    public void bind(LogicalChannel channel) {
        QName deployable = channel.getParent().getDeployable();
        String topic = channel.getUri().toString();
        JmsBindingDefinition channelDefinition = createTopicBindingDefinition(topic);
        LogicalBinding<JmsBindingDefinition> channelBinding = new LogicalBinding<>(channelDefinition, channel, deployable);
        channelBinding.setAssigned(true);
        channel.addBinding(channelBinding);
    }

    private URI createCallbackUri(LogicalService service) {
        LogicalComponent<?> component = service.getParent();
        String name = service.getServiceContract().getCallbackContract().getInterfaceName();
        return URI.create(component.getUri() + "#" + name + "Callback");
    }

    private boolean isRequestResponse(ServiceContract targetContract) {
        for (Operation operation : targetContract.getOperations()) {
            if (!operation.isOneWay()) {
                return true;
            }
        }
        return false;
    }

    private JmsBindingDefinition createBindingDefinition(String queueName, boolean response, boolean xa) {
        JmsBindingMetadata metadata = new JmsBindingMetadata();
        metadata.setCacheLevel(level);
        DestinationDefinition destinationDefinition = new DestinationDefinition();
        destinationDefinition.setType(DestinationType.QUEUE);
        destinationDefinition.setCreate(CreateOption.IF_NOT_EXIST);
        destinationDefinition.setName(queueName);
        metadata.setDestination(destinationDefinition);
        ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
        if (xa && xaConnectionFactory != null) {
            // XA connection factory defined
            factoryDefinition.setName(xaConnectionFactory);
            factoryDefinition.setCreate(CreateOption.NEVER);
            metadata.setConnectionFactory(factoryDefinition);
        } else if (!xa && connectionFactory != null) {
            // non-XA connection factory defined
            factoryDefinition.setName(connectionFactory);
            factoryDefinition.setCreate(CreateOption.NEVER);
            metadata.setConnectionFactory(factoryDefinition);
        }

        if (response) {
            DestinationDefinition responseDestinationDefinition = new DestinationDefinition();
            responseDestinationDefinition.setType(DestinationType.QUEUE);
            responseDestinationDefinition.setCreate(CreateOption.IF_NOT_EXIST);
            responseDestinationDefinition.setName(queueName + "Response");
            ResponseDefinition responseDefinition = new ResponseDefinition();
            responseDefinition.setConnectionFactory(factoryDefinition);
            responseDefinition.setDestination(responseDestinationDefinition);
            metadata.setResponse(responseDefinition);
        }

        JmsBindingDefinition definition = new JmsBindingDefinition(metadata);
        definition.setJmsMetadata(metadata);
        definition.setName("bindingSCAJMS");
        return definition;
    }

    private JmsBindingDefinition createTopicBindingDefinition(String topicName) {
        JmsBindingMetadata metadata = new JmsBindingMetadata();
        metadata.setCacheLevel(level);
        DestinationDefinition destinationDefinition = new DestinationDefinition();
        destinationDefinition.setType(DestinationType.TOPIC);
        destinationDefinition.setCreate(CreateOption.IF_NOT_EXIST);
        destinationDefinition.setName(topicName);
        metadata.setDestination(destinationDefinition);

        if (connectionFactory != null) {
            // non-XA connection factory defined
            ConnectionFactoryDefinition factoryDefinition = new ConnectionFactoryDefinition();
            factoryDefinition.setName(connectionFactory);
            factoryDefinition.setCreate(CreateOption.NEVER);
            metadata.setConnectionFactory(factoryDefinition);
        }
        JmsBindingDefinition definition = new JmsBindingDefinition(metadata);
        definition.setJmsMetadata(metadata);
        definition.setName("bindingSCAJMS");
        return definition;
    }

    /**
     * Recurses the component hierarchy to determine if XA transacted messaging is required.
     *
     * @param service the service or reference
     * @return true if XA is required
     */
    private boolean isXA(LogicalService service) {
        LogicalComponent<?> parent = service.getParent();
        while (parent != null) {
            if (isTransactional(parent)) {
                return true;
            }
            if (isTransactional(parent)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;

    }

    private boolean isTransactional(LogicalComponent<?> component) {
        List<String> policies = component.getDefinition().getComponentType().getPolicies();
        return policies.contains(MANAGED_TRANSACTION) || policies.contains(MANAGED_TRANSACTION_GLOBAL) || policies.contains(MANAGED_TRANSACTION_LOCAL);
    }

    /**
     * Determines if the target should be bound, i.e. if it has not already been bound by binding.sca or is remote (and not hosted on the current runtime).
     *
     * @param target the target
     * @return true if the target should be bound
     */
    private boolean bindTarget(LogicalService target) {
        if (target.getServiceContract() instanceof RemoteServiceContract) {
            return false;
        }
        for (LogicalBinding<?> binding : target.getBindings()) {
            if (binding.isAssigned()) {
                return false;
            }
        }
        return true;
    }

}
