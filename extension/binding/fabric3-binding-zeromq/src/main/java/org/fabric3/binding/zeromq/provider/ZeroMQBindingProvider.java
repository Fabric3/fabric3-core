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
package org.fabric3.binding.zeromq.provider;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.binding.BindingMatchResult;
import org.fabric3.spi.domain.generator.binding.BindingProvider;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Property;

/**
 * A binding.sca provider that uses ZeroMQ as the underlying transport.
 */
public class ZeroMQBindingProvider implements BindingProvider {
    private static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.zeromq");

    private static final BindingMatchResult MATCH = new BindingMatchResult(true, BINDING_QNAME);
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, BINDING_QNAME);

    private boolean enabled = true;
    private long highWater = -1;
    private long multicastRate = -1;
    private long multicastRecovery = -1;
    private long sendBuffer = -1;
    private long receiveBuffer = -1;

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Property(required = false)
    public void setHighWater(long highWater) {
        this.highWater = highWater;
    }

    @Property(required = false)
    public void setMulticastRate(long multicastRate) {
        this.multicastRate = multicastRate;
    }

    @Property(required = false)
    public void setMulticastRecovery(long multicastRecovery) {
        this.multicastRecovery = multicastRecovery;
    }

    @Property(required = false)
    public void setSendBuffer(long sendBuffer) {
        this.sendBuffer = sendBuffer;
    }

    @Property(required = false)
    public void setReceiveBuffer(long receiveBuffer) {
        this.receiveBuffer = receiveBuffer;
    }

    public QName getType() {
        return BINDING_QNAME;
    }

    public BindingMatchResult canBind(LogicalWire wire) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public BindingMatchResult canBind(LogicalService service) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public void bind(LogicalService service) {
        QName deployable = service.getParent().getDeployable();

        ZeroMQMetadata metadata = createMetadata();
        ZeroMQBinding bindingDefinition = new ZeroMQBinding("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBinding> serviceBinding = new LogicalBinding<>(bindingDefinition, service, deployable);
        serviceBinding.setAssigned(true);
        service.addBinding(serviceBinding);

        // check if the interface is bidirectional
        ServiceContract targetContract = service.getDefinition().getServiceContract();
        if (targetContract.getCallbackContract() != null) {
            // setup callback bindings
            ZeroMQMetadata callbackMetadata = createMetadata();

            ZeroMQBinding callbackBindingDefinition = new ZeroMQBinding("binding.zeromq.callback", callbackMetadata);
            LogicalBinding<ZeroMQBinding> callbackBinding = new LogicalBinding<>(callbackBindingDefinition, service, deployable);
            callbackBinding.setAssigned(true);
            service.addCallbackBinding(callbackBinding);
        }

    }

    public void bind(LogicalWire wire) throws Fabric3Exception {
        LogicalReference source = wire.getSource().getLeafReference();
        LogicalService target = wire.getTarget().getLeafService();
        QName deployable = source.getParent().getDeployable();

        ZeroMQMetadata metadata = createMetadata();

        // setup the forward binding
        ZeroMQBinding referenceBindingDefinition = new ZeroMQBinding("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBinding> referenceBinding = new LogicalBinding<>(referenceBindingDefinition, source, deployable);
        referenceBindingDefinition.setTargetUri(URI.create(UriHelper.getBaseName(target.getUri())));
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);

        boolean bindTarget = bindTarget(target);

        if (bindTarget) {
            ZeroMQBinding serviceBindingDefinition = new ZeroMQBinding("binding.zeromq", metadata);
            LogicalBinding<ZeroMQBinding> serviceBinding = new LogicalBinding<>(serviceBindingDefinition, target, deployable);
            serviceBinding.setAssigned(true);
            target.addBinding(serviceBinding);
        }

        // check if the interface is bidirectional
        ServiceContract targetContract = target.getDefinition().getServiceContract();
        if (targetContract.getCallbackContract() != null) {
            // setup callback bindings
            ZeroMQMetadata callbackMetadata = createMetadata();

            ZeroMQBinding callbackReferenceBindingDefinition = new ZeroMQBinding("binding.zeromq.callback", callbackMetadata);
            LogicalBinding<ZeroMQBinding> callbackReferenceBinding = new LogicalBinding<>(callbackReferenceBindingDefinition, source, deployable);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);

            if (bindTarget) {
                ZeroMQBinding callbackServiceBindingDefinition = new ZeroMQBinding("binding.zeromq.callback", callbackMetadata);
                LogicalBinding<ZeroMQBinding> callbackServiceBinding = new LogicalBinding<>(callbackServiceBindingDefinition, target, deployable);
                callbackServiceBinding.setAssigned(true);
                target.addCallbackBinding(callbackServiceBinding);
            }
        }
    }

    public void bind(LogicalChannel channel) throws Fabric3Exception {
        ZeroMQMetadata metadata = createMetadata();
        metadata.setChannelName(channel.getDefinition().getName());
        ZeroMQBinding definition = new ZeroMQBinding("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBinding> binding = new LogicalBinding<>(definition, channel);
        channel.addBinding(binding);
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

    private ZeroMQMetadata createMetadata() {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        metadata.setHighWater(highWater);
        metadata.setMulticastRate(multicastRate);
        metadata.setMulticastRecovery(multicastRecovery);
        metadata.setReceiveBuffer(receiveBuffer);
        metadata.setSendBuffer(sendBuffer);
        return metadata;
    }

}
