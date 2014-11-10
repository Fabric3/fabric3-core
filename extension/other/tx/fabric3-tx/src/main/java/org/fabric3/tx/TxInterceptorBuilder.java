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
package org.fabric3.tx;

import javax.transaction.TransactionManager;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Creates a TxInterceptor for a wire invocation chain.
 */
public class TxInterceptorBuilder implements InterceptorBuilder<TxInterceptorDefinition> {
    private TransactionManager transactionManager;
    private TxMonitor monitor;

    public TxInterceptorBuilder(@Reference TransactionManager transactionManager, @Monitor TxMonitor monitor) {
        this.transactionManager = transactionManager;
        this.monitor = monitor;
    }

    public Interceptor build(TxInterceptorDefinition interceptorDefinition) throws ContainerException {
        return new TxInterceptor(transactionManager, interceptorDefinition.getAction(), monitor);
    }


}
