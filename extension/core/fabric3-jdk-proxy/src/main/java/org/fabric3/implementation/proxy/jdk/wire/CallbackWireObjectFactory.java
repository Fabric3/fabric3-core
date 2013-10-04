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
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
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

    public T getInstance() throws ObjectCreationException {
        if (multiThreaded) {
            try {
                return interfaze.cast(proxyService.createMultiThreadedCallbackProxy(interfaze, mappings));
            } catch (ProxyCreationException e) {
                throw new ObjectCreationException(e);
            }
        } else {
            CallbackReference callbackReference = WorkContextCache.getThreadWorkContext().peekCallbackReference();
            String callbackUri = callbackReference.getServiceUri();
            Map<Method, InvocationChain> mapping = (singleMapping != null) ? singleMapping : mappings.get(callbackUri);
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
