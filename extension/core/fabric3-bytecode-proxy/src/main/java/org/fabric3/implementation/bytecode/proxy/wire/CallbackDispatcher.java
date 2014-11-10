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
package org.fabric3.implementation.bytecode.proxy.wire;

import java.util.Map;

import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback invocation from multi-threaded component instances such as composite scope components.
 * <p/>
 * Since callback proxies for multi-threaded components may dispatch to multiple callback services, this implementation must determine the correct target
 * service based on the current callback reference. For example, if clients A and A' implementing the same callback interface C invoke B, the callback proxy
 * representing C must correctly dispatch back to A and A'. This is done by recording the callback URI as the forward invoke is made.
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
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        CallbackReference callbackReference = workContext.peekCallbackReference();
        String callbackUri = callbackReference.getServiceUri();

        // find the callback invocation chain for the invoked operation
        InvocationChain[] chains = mappings.get(callbackUri);
        InvocationChain chain = chains[i];
        return super.invoke(chain, args, workContext);
    }

}
