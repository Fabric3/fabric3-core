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
package org.fabric3.binding.activemq.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.common.CacheLevel;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.CreateOption;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.DestinationType;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.ResponseDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.binding.BindingMatchResult;
import org.fabric3.spi.generator.binding.BindingProvider;
import org.fabric3.spi.generator.binding.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;

/**
 * Implements binding.sca using ActiveMQ.
 * <p/>
 * <p/>
 * By default, this provider uses an embedded broker, which forwards messages to peer brokers in a zone. To configure the provider to use a remote
 * broker, the <code>brokerUrl</code> property may be set to the appropriate broker location.
 * <p/>
 * <p/>
 * Also, the provider uses default connection factory configurations; to use specific connection factories, set the <code>connectionFactory</code> and
 * <code>xaConnectionFactory</code> properties.
 */
@EagerInit
public class ActiveMQBindingProvider implements BindingProvider {
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, JmsBindingDefinition.BINDING_QNAME);

    private static final QName OASIS_TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName OASIS_ONEWAY = new QName(Constants.SCA_NS, "oneWay");

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
        // TODO handle must provide intents
        return new BindingMatchResult(true, getType());
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        if (!enabled) {
            return NO_MATCH;
        }
        // TODO handle must provide intents
        return new BindingMatchResult(true, getType());
    }

    public BindingMatchResult canBind(LogicalService service) {
        if (!enabled) {
            return NO_MATCH;
        }
        // TODO handle must provide intents
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
        LogicalBinding<JmsBindingDefinition> referenceBinding = new LogicalBinding<JmsBindingDefinition>(referenceDefinition, source, deployable);
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);

        boolean xa = isXA(target, false);
        JmsBindingDefinition serviceDefinition = createBindingDefinition(forwardQueue, response, xa);
        LogicalBinding<JmsBindingDefinition> serviceBinding = new LogicalBinding<JmsBindingDefinition>(serviceDefinition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);

        // check if the interface is bidirectional
        if (targetContract.getCallbackContract() != null) {
            // setup callback bindings
            // derive the callback queue name from the reference name since multiple clients can connect to a service
            String callbackQueue = source.getUri().toString();
            boolean callbackXa = isXA(target, true);

            JmsBindingDefinition callbackReferenceDefinition = createBindingDefinition(callbackQueue, false, callbackXa);
            LogicalBinding<JmsBindingDefinition> callbackReferenceBinding =
                    new LogicalBinding<JmsBindingDefinition>(callbackReferenceDefinition, source, deployable);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);
            JmsBindingDefinition callbackServiceDefinition =
                    createBindingDefinition(callbackQueue, false, false); // XA not enabled on service side callback
            LogicalBinding<JmsBindingDefinition> callbackServiceBinding =
                    new LogicalBinding<JmsBindingDefinition>(callbackServiceDefinition, target, deployable);
            callbackServiceBinding.setAssigned(true);
            target.addCallbackBinding(callbackServiceBinding);
            callbackReferenceDefinition.setGeneratedTargetUri(createCallbackUri(source));
            callbackServiceDefinition.setGeneratedTargetUri(createCallbackUri(source));
        }
    }

    public void bind(LogicalService service) throws BindingSelectionException {
        throw new UnsupportedOperationException();
    }

    private boolean isRequestResponse(ServiceContract targetContract) {
        for (Operation operation : targetContract.getOperations()) {
            if (!operation.getIntents().contains(OASIS_ONEWAY)) {
                return true;
            }
        }
        return false;
    }

    public void bind(LogicalChannel channel) {
        QName deployable = channel.getParent().getDeployable();
        String topic = channel.getUri().toString();
        JmsBindingDefinition channelDefinition = createTopicBindingDefinition(topic);
        LogicalBinding<JmsBindingDefinition> channelBinding = new LogicalBinding<JmsBindingDefinition>(channelDefinition, channel, deployable);
        channelBinding.setAssigned(true);
        channel.addBinding(channelBinding);
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

    public URI createCallbackUri(LogicalReference source) {
        LogicalComponent<?> component = source.getParent();
        String name = source.getDefinition().getServiceContract().getCallbackContract().getInterfaceName();
        return URI.create(component.getUri() + "#" + name);
    }

    /**
     * Recurses the component hierarchy to determine if XA transacted messaging is required.
     * <p/>
     * TODO this should be refactored to normalize intents
     *
     * @param service  the service or reference
     * @param callback true if callback operations should be evaluated
     * @return true if XA is required
     */
    private boolean isXA(LogicalService service, boolean callback) {
        // check operations
        if (callback) {
            for (LogicalOperation operation : service.getCallbackOperations()) {
                if (containsTransactionIntent(operation.getIntents())) {
                    return true;
                }
            }
        } else {
            for (LogicalOperation operation : service.getOperations()) {
                if (containsTransactionIntent(operation.getIntents())) {
                    return true;
                }
            }
        }
        // recurse the parents
        LogicalComponent<?> parent = service.getParent();
        while (parent != null) {
            if (containsTransactionIntent(parent.getIntents())) {
                return true;
            }
            if (containsTransactionIntent(parent.getDefinition().getImplementation().getIntents())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;

    }

    private boolean containsTransactionIntent(Set<QName> intents) {
        return intents.contains(OASIS_TRANSACTED_ONEWAY);
    }

}
