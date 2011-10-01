/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.zeromq.provider;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Property;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * A binding.sca provider that uses ZeroMQ as the underlying transport.
 *
 * @version $Rev$ $Date$
 */
public class ZeroMQBindingProvider implements BindingProvider {
    private static final BindingMatchResult MATCH = new BindingMatchResult(true, ZeroMQBindingDefinition.BINDING_0MQ);
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, ZeroMQBindingDefinition.BINDING_0MQ);

    private boolean enabled = true;
    private long highWater = -1;
    private long multicastRate = -1;
    private long multicastRecovery = -1;
    private long sendBuffer = -1;
    private long receiveBuffer = -1;
    private String host;

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

    @Property(required = false)
    public void setHost(String host) {
        this.host = host;
    }

    public QName getType() {
        return ZeroMQBindingDefinition.BINDING_0MQ;
    }

    public BindingMatchResult canBind(LogicalWire wire) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public void bind(LogicalWire wire) throws BindingSelectionException {
        LogicalReference source = wire.getSource().getLeafReference();
        LogicalService target = wire.getTarget().getLeafService();
        QName deployable = source.getParent().getDeployable();

        ZeroMQMetadata metadata = createMetadata();

        // setup the forward binding
        ZeroMQBindingDefinition referenceDefinition = new ZeroMQBindingDefinition("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBindingDefinition> referenceBinding =
                new LogicalBinding<ZeroMQBindingDefinition>(referenceDefinition, source, deployable);
        referenceDefinition.setTargetUri(URI.create(UriHelper.getBaseName(target.getUri())));
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);


        ZeroMQBindingDefinition serviceDefinition = new ZeroMQBindingDefinition("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBindingDefinition> serviceBinding = new LogicalBinding<ZeroMQBindingDefinition>(serviceDefinition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);

        // check if the interface is bidirectional
        ServiceContract targetContract = target.getDefinition().getServiceContract();
        if (targetContract.getCallbackContract() != null) {
            // setup callback bindings
            ZeroMQMetadata callbackMetadata = createMetadata();

            ZeroMQBindingDefinition callbackReferenceDefinition = new ZeroMQBindingDefinition("binding.zeromq.callback", callbackMetadata);
            LogicalBinding<ZeroMQBindingDefinition> callbackReferenceBinding =
                    new LogicalBinding<ZeroMQBindingDefinition>(callbackReferenceDefinition, source, deployable);
            callbackReferenceBinding.setAssigned(true);
            source.addCallbackBinding(callbackReferenceBinding);

            ZeroMQBindingDefinition callbackServiceDefinition = new ZeroMQBindingDefinition("binding.zeromq.callback", callbackMetadata);
            LogicalBinding<ZeroMQBindingDefinition> callbackServiceBinding =
                    new LogicalBinding<ZeroMQBindingDefinition>(callbackServiceDefinition, target, deployable);
            callbackServiceBinding.setAssigned(true);
            target.addCallbackBinding(callbackServiceBinding);
        }
    }

    public void bind(LogicalChannel channel) throws BindingSelectionException {
        ZeroMQMetadata metadata = createMetadata();
        metadata.setChannelName(channel.getDefinition().getName());
        ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBindingDefinition> binding = new LogicalBinding<ZeroMQBindingDefinition>(definition, channel);
        channel.addBinding(binding);
    }

    private ZeroMQMetadata createMetadata() {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        metadata.setHighWater(highWater);
        metadata.setMulticastRate(multicastRate);
        metadata.setMulticastRecovery(multicastRecovery);
        metadata.setReceiveBuffer(receiveBuffer);
        metadata.setSendBuffer(sendBuffer);
        metadata.setHost(host);
        return metadata;
    }


}
