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
 */
package org.fabric3.implementation.bytecode.proxy.channel;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.implementation.bytecode.proxy.common.ProxyException;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates a proxy for a channel.
 */
public class ChannelProxyObjectFactory<T> implements ObjectFactory<T> {
    private ProxyFactory proxyFactory;
    private URI uri;
    private Class<T> interfaze;
    private Method[] methods;
    private EventStreamHandler handler;

    private T proxy;

    public ChannelProxyObjectFactory(URI uri, Class<T> interfaze, Method method, EventStreamHandler handler, ProxyFactory proxyFactory) {
        this.uri = uri;
        this.interfaze = interfaze;
        this.methods = new Method[]{method};
        this.handler = handler;
        this.proxyFactory = proxyFactory;
    }

    public T getInstance() throws ObjectCreationException {
        try {
            if (proxy == null) {
                proxy = proxyFactory.createProxy(uri, interfaze, methods, ChannelProxyDispatcher.class, false);
                ((ChannelProxyDispatcher) proxy).init(handler);
            }
            return proxy;
        } catch (ProxyException e) {
            throw new ObjectCreationException(e);
        }
    }
}
