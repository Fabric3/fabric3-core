/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.implementation.bytecode.proxy.common.ProxyException;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Creates proxies for a callback wire.
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private URI uri;
    private Class<T> interfaze;
    private Method[] methods;
    private Map<String, InvocationChain[]> chains;
    private ProxyFactory proxyFactory;

    private T proxy;

    public CallbackWireObjectFactory(URI uri,
                                     Class<T> interfaze,
                                     Method[] methods,
                                     String callbackUri,
                                     InvocationChain[] invocationChains,
                                     ProxyFactory proxyFactory) {
        this.uri = uri;
        this.interfaze = interfaze;

        this.methods = methods;
        this.chains = new HashMap<>();
        this.chains.put(callbackUri, invocationChains);
        this.proxyFactory = proxyFactory;
    }

    public T getInstance() throws ObjectCreationException {
        if (proxy != null) {
            return proxy;
        }
        try {
            if (chains.size() == 1) {
                // if the component is only one callback, there will only be one invocation chain; use and optimized dispatcher
                OptimizedCallbackDispatcher dispatcher = (OptimizedCallbackDispatcher) proxyFactory.createProxy(uri,
                                                                                                                interfaze,
                                                                                                                methods,
                                                                                                                OptimizedCallbackDispatcher.class,
                                                                                                                true);
                dispatcher.init(chains.values().iterator().next());
                return interfaze.cast(dispatcher);
            } else {
                CallbackDispatcher dispatcher = (CallbackDispatcher) proxyFactory.createProxy(uri, interfaze, methods, CallbackDispatcher.class, true);
                dispatcher.init(chains);
                return interfaze.cast(dispatcher);
            }
        } catch (ProxyException e) {
            throw new ObjectCreationException(e);
        }
    }

    public void updateMappings(String callbackUri, InvocationChain[] invocationChains) {
        chains.put(callbackUri, invocationChains);
        proxy = null;
    }

}
