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
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.implementation.pojo.spi.proxy.WireProxyServiceExtension;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Creates JDK-based wire proxies.
 */
public interface JDKWireProxyService extends WireProxyServiceExtension {

    /**
     * Creates a Java proxy for the given wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param callbackUri the callback URI fr the wire fronted by the proxy or null if the wire is unidirectional
     * @param mappings    the method to invocation chain mappings
     * @return the proxy
     * @throws ContainerException if there was a problem creating the proxy
     */
    <T> T createProxy(Class<T> interfaze, String callbackUri, Map<Method, InvocationChain> mappings) throws ContainerException;

    /**
     * Creates a Java proxy for the callback invocations chains.
     *
     * @param interfaze the interface the proxy should implement
     * @param mappings  the invocation chain mappings keyed by target URI @return the proxy
     * @return the proxy instance
     * @throws ContainerException if an error is encountered during proxy generation
     */
    <T> T createMultiThreadedCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) throws ContainerException;

    /**
     * Creates a callback proxy that always returns to the same target service
     *
     * @param interfaze the service interface
     * @param mapping   the invocation chain mapping for the callback service
     * @return the proxy instance
     */
    <T> T createCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping);

}
