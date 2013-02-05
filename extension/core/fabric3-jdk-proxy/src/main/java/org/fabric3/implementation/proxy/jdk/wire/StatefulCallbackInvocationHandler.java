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
*/
package org.fabric3.implementation.proxy.jdk.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback service from a component implementation instance that is not composite scope. Since only one client can
 * invoke the instance this proxy is injected on at a time, there can only be one callback target, even if the proxy is injected on an instance
 * variable. Consequently, the proxy does not need to map the callback target based on the forward request.
 */
public class StatefulCallbackInvocationHandler<T> extends AbstractCallbackInvocationHandler<T> {
    private Map<Method, InvocationChain> chains;

    /**
     * Constructor.
     *
     * @param interfaze      the callback service interface implemented by the proxy
     * @param chains         the invocation chain mappings for the callback wire
     */
    public StatefulCallbackInvocationHandler(Class<T> interfaze, Map<Method, InvocationChain> chains) {
        super(interfaze);
        this.chains = chains;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }
        return super.invoke(chain, args, workContext);
    }

}