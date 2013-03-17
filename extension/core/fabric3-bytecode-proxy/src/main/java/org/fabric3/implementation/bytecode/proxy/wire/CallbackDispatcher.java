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
package org.fabric3.implementation.bytecode.proxy.wire;

import java.util.Map;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback invocation from multi-threaded component instances such as composite scope components.
 * <p/>
 * Since callback proxies for multi-threaded components may dispatch to multiple callback services, this implementation must determine the correct target
 * service based on the current CallFrame. For example, if clients A and A' implementing the same callback interface C invoke B, the callback proxy representing
 * C must correctly dispatch back to A and A'. This is done by recording the callback URI in the current CallFrame as the forward invoke is made.
 */
public class CallbackDispatcher extends AbstractCallbackDispatcher {
    private Map<String, InvocationChain[]> mappings;

    /**
     * In multi-threaded instances such as composite scoped components, multiple forward invocations may be received simultaneously. As a result, since callback
     * proxies stored in instance variables may represent multiple clients, they must map the correct one for the request being processed on the current thread.
     * The mappings parameter keys a callback URI representing the client to the set of invocation chains for the callback service.
     *
     * @param mappings the callback URI to invocation chain mappings
     */
    public void init(Map<String, InvocationChain[]> mappings) {
        this.mappings = mappings;
    }

    public Object _f3_invoke(int i, Object args) throws Throwable {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        CallFrame frame = workContext.peekCallFrame();
        String callbackUri = frame.getCallbackUri();

        // find the callback invocation chain for the invoked operation
        InvocationChain[] chains = mappings.get(callbackUri);
        InvocationChain chain = chains[i];
        return super.invoke(chain, args, workContext);
    }

}
