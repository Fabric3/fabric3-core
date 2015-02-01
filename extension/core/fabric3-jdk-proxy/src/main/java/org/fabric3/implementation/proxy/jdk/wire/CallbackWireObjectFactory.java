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
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Returns a proxy instance for a callback wire.
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private Class<T> interfaze;
    private boolean multiThreaded;
    private JDKWireProxyService proxyService;
    private Map<String, Map<Method, InvocationChain>> mappings;
    private Map<Method, InvocationChain> singleMapping;

    /**
     * Constructor.
     *
     * @param interfaze     the proxy interface
     * @param multiThreaded if the proxy must be thread safe
     * @param proxyService  the service for creating proxies
     * @param mappings      the callback URI to invocation chain mappings
     */
    public CallbackWireObjectFactory(Class<T> interfaze,
                                     boolean multiThreaded,
                                     JDKWireProxyService proxyService,
                                     Map<String, Map<Method, InvocationChain>> mappings) {
        this.interfaze = interfaze;
        this.multiThreaded = multiThreaded;
        this.proxyService = proxyService;
        this.mappings = mappings;
        if (mappings.size() == 1) {
            singleMapping = mappings.values().iterator().next();
        }
    }

    public T getInstance() throws Fabric3Exception {
        if (multiThreaded) {
            return interfaze.cast(proxyService.createMultiThreadedCallbackProxy(interfaze, mappings));
        } else {
            String callbackReference = WorkContextCache.getThreadWorkContext().peekCallbackReference();
            Map<Method, InvocationChain> mapping = (singleMapping != null) ? singleMapping : mappings.get(callbackReference);
            return interfaze.cast(proxyService.createCallbackProxy(interfaze, mapping));
        }
    }

    public void updateMappings(String callbackUri, Map<Method, InvocationChain> chains) {
        mappings.put(callbackUri, chains);
        if (mappings.size() == 1) {
            singleMapping = mappings.values().iterator().next();
        } else {
            singleMapping = null;
        }
    }

}
