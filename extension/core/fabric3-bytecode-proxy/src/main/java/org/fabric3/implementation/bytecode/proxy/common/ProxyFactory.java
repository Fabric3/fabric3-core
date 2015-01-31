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
package org.fabric3.implementation.bytecode.proxy.common;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.api.host.ContainerException;

/**
 * Creates a bytecode generated proxy that dispatches to a target. <p/> Bytecode proxies are designed to be more performant than traditional JDK proxies as they
 * dispatch based on a method index.
 */
public interface ProxyFactory {

    /**
     * Creates a proxy.
     *
     * @param classLoaderKey the key of the classloader the proxy is to be created for
     * @param interfaze      the proxy interface
     * @param methods        the sorted list of proxy methods. If multiple proxies are created for a classloader, method order must be the same as proxy
     *                       bytecode is cached.
     * @param dispatcher     the dispatcher the proxy extends
     * @param wrapped        true if parameters should be wrapped in an array as JDK proxy invocations are
     * @return the proxy instance, which extends the provided dispatcher class
     * @throws ContainerException if there is an error creating the proxy
     */
    <T> T createProxy(URI classLoaderKey, Class<T> interfaze, Method[] methods, Class<? extends ProxyDispatcher> dispatcher, boolean wrapped)
            throws ContainerException;

}
