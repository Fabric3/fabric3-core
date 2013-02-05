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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.spi;

import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.oasisopen.sca.ServiceReference;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

/**
 * Creates proxies that implement Java interfaces and invocation handlers for fronting wires.
 */

public interface WireProxyService {
    /**
     * Create an ObjectFactory that provides proxies for the forward wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param wire        the wire to proxy @return an ObjectFactory that will create proxies
     * @param callbackUri the callback URI or null if the wire is unidirectional
     * @return the factory
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ProxyCreationException;

    /**
     * Create an ObjectFactory that provides proxies for the callback wire.
     *
     * @param interfaze     the interface the proxy implements
     * @param multiThreaded if the proxy should be thread-safe
     * @param callbackUri   the callback service uri
     * @param wire          the wire to proxy
     * @return an ObjectFactory that will create proxies
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException;

    /**
     * Updates an ObjectFactory with an additional callback wire. This is used when multiple clients are wired to a target bidirectional service.
     *
     * @param factory       the ObjectFactory to update
     * @param interfaze     the interface the proxy implements
     * @param multiThreaded if the proxy should be thread-safe
     * @param callbackUri   the callback service uri
     * @param wire          the wire to proxy
     * @return an ObjectFactory that will create proxies
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory,
                                                     Class<T> interfaze,
                                                     boolean multiThreaded,
                                                     URI callbackUri,
                                                     Wire wire) throws ProxyCreationException;


    /**
     * Creates a Java proxy for the given wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param callbackUri the callback URI fr the wire fronted by the proxy or null if the wire is unidirectional
     * @param mappings    the method to invocation chain mappings
     * @return the proxy
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> T createProxy(Class<T> interfaze, String callbackUri, Map<Method, InvocationChain> mappings) throws ProxyCreationException;

    /**
     * Creates a Java proxy for the callback invocations chains.
     *
     * @param interfaze the interface the proxy should implement
     * @param mappings  the invocation chain mappings keyed by target URI @return the proxy
     * @return the proxy instance
     * @throws ProxyCreationException if an error is encountered during proxy generation
     */
    <T> T createMultiThreadedCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) throws ProxyCreationException;

    /**
     * Creates a callback proxy that always returns to the same target service
     *
     * @param interfaze the service interface
     * @param mapping   the invocation chain mapping for the callback service
     * @return the proxy instance
     */
    <T> T createCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping);

    /**
     * Cast a proxy to a ServiceReference.
     *
     * @param target a proxy generated by this implementation
     * @return a ServiceReference equivalent to this proxy
     * @throws IllegalArgumentException if the object supplied is not a proxy
     */
    <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException;

}
