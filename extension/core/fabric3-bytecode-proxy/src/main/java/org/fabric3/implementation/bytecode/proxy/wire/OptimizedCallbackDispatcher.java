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

import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback invocation from a stateless component or component with only one callback client..
 */
public class OptimizedCallbackDispatcher extends AbstractCallbackDispatcher {
    private InvocationChain[] chains;

    public void init(InvocationChain[] chains) {
        this.chains = chains;
    }

    public Object _f3_invoke(int i, Object args) throws Throwable {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains[i];
        // find the invocation chain for the invoked operation
        return super.invoke(chain, args, workContext);
    }

}
