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
*/
package org.fabric3.binding.test;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class BindingChannelImpl implements BindingChannel {
    private Map<URI, Holder> wires = new ConcurrentHashMap<URI, Holder>();

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
            CallFrame previous = workContext.peekCallFrame();
            // copy correlation information from incoming frame
            Serializable id = previous.getCorrelationId(Serializable.class);
            String callbackUri = holder.getCallbackUri();
            CallFrame frame = new CallFrame(callbackUri, id);
            workContext.addCallFrame(frame);
            return chain.getHeadInterceptor().invoke(msg);
        } finally {
            workContext.popCallFrame();
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
