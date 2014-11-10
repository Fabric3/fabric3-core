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
package org.fabric3.binding.test;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class BindingChannelImpl implements BindingChannel {
    private Map<URI, Holder> wires = new ConcurrentHashMap<>();

    public void registerDestinationWire(URI uri, Wire wire, URI callbackUri) {
        wires.put(uri, new Holder(wire, callbackUri));
    }

    public Message send(URI destination, String operation, Message msg) {
        Holder holder = wires.get(destination);
        if (holder == null) {
            throw new ServiceUnavailableException("No destination registered for [" + destination + "]");
        }
        Wire wire = holder.getWire();
        InvocationChain chain = null;
        for (InvocationChain invocationChain : wire.getInvocationChains()) {
            if (invocationChain.getPhysicalOperation().getName().equals(operation)) {
                chain = invocationChain;
            }
        }
        if (chain == null) {
            throw new ServiceRuntimeException("Operation on " + destination + " not found [" + operation + "]");
        }
        WorkContext workContext = msg.getWorkContext();
        try {
            return chain.getHeadInterceptor().invoke(msg);
        } finally {
            workContext.popCallbackReference();
        }
    }

    private class Holder {
        private Wire wire;
        private String callbackUri;

        public Wire getWire() {
            return wire;
        }

        public String getCallbackUri() {
            return callbackUri;
        }

        private Holder(Wire wire, URI callbackUri) {
            this.wire = wire;
            if (callbackUri != null) {
                this.callbackUri = callbackUri.toString();
            }
        }
    }
}
