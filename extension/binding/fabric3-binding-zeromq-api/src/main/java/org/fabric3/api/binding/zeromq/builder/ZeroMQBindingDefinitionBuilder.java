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
