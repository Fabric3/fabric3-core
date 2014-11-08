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
package org.fabric3.api.binding.zeromq.builder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 * Builder for the ZeroMQ binding.
 */
public class ZeroMQBindingDefinitionBuilder extends AbstractBuilder {
    private ZeroMQBindingDefinition binding;

    public static ZeroMQBindingDefinitionBuilder newBuilder() {
        return new ZeroMQBindingDefinitionBuilder();
    }

    public ZeroMQBindingDefinitionBuilder() {
        this("zeromq.binding");
    }

    public ZeroMQBindingDefinitionBuilder(String name) {
        this.binding = new ZeroMQBindingDefinition(name, new ZeroMQMetadata());
    }

    public ZeroMQBindingDefinition build() {
        checkState();
        freeze();
        return binding;
    }

    public ZeroMQBindingDefinitionBuilder target(URI target) {
        checkState();
        binding.setTargetUri(target);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder address(List<String> addresses) {
        checkState();
        List<SocketAddressDefinition> list = new ArrayList<>();
        for (String address : addresses) {
            list.add(new SocketAddressDefinition(address));
        }
        binding.getZeroMQMetadata().setSocketAddresses(list);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder sendBuffer(long value) {
        checkState();
        binding.getZeroMQMetadata().setSendBuffer(value);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder wireFormat(String value) {
        checkState();
        binding.getZeroMQMetadata().setWireFormat(value);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder multicastRecovery(long value) {
        checkState();
        binding.getZeroMQMetadata().setMulticastRecovery(value);
        return this;
    }

    public ZeroMQBindingDefinition receiveBuffer(long value) {
        checkState();
        binding.getZeroMQMetadata().setReceiveBuffer(value);
        return binding;
    }

    public ZeroMQBindingDefinitionBuilder highWater(long value) {
        checkState();
        binding.getZeroMQMetadata().setHighWater(value);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder multicastRate(long value) {
        checkState();
        binding.getZeroMQMetadata().setMulticastRate(value);
        return this;
    }

    public ZeroMQBindingDefinitionBuilder timeout(long value) {
        checkState();
        binding.getZeroMQMetadata().setTimeout(value);
        return this;
    }

}
