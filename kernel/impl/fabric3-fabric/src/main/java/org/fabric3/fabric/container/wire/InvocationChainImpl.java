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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.wire;

import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * Default implementation of an invocation chain.
 */
public class InvocationChainImpl implements InvocationChain {
    protected PhysicalOperationDefinition physicalOperation;
    protected Interceptor interceptorChainHead;
    protected Interceptor interceptorChainTail;

    public InvocationChainImpl(PhysicalOperationDefinition operation) {
        this.physicalOperation = operation;
    }

    public PhysicalOperationDefinition getPhysicalOperation() {
        return physicalOperation;
    }

    public void addInterceptor(Interceptor interceptor) {
        if (interceptorChainHead == null) {
            interceptorChainHead = interceptor;
        } else {
            interceptorChainTail.setNext(interceptor);
        }
        interceptorChainTail = interceptor;
    }

    public void addInterceptor(int index, Interceptor interceptor) {
        int i = 0;
        Interceptor next = interceptorChainHead;
        Interceptor prev = null;
        while (next != null && i < index) {
            prev = next;
            next = next.getNext();
            i++;
        }
        if (i == index) {
            if (prev != null) {
                prev.setNext(interceptor);
            } else {
                interceptorChainHead = interceptor;
            }
            interceptor.setNext(next);
            if (next == null) {
                interceptorChainTail = interceptor;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public Interceptor getHeadInterceptor() {
        return interceptorChainHead;
    }

    public Interceptor getTailInterceptor() {
        return interceptorChainTail;
    }

}
