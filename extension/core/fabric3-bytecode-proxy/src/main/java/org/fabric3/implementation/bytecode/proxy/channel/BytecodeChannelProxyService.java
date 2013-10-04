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
 *
 */
package org.fabric3.implementation.bytecode.proxy.channel;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.host.Names;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyServiceExtension;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that delegates to a {@link ProxyFactory} to create channel proxies.
 */
public class BytecodeChannelProxyService implements ChannelProxyServiceExtension {
    private ProxyFactory proxyFactory;

    public BytecodeChannelProxyService(@Reference ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public boolean isDefault() {
        return false;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) throws ProxyCreationException {
        URI uri = getClassLoaderUri(interfaze);

        Method[] methods = interfaze.getMethods();
        if (methods.length > 1) {
            throw new ProxyCreationException("Channel interface must have only one method: " + interfaze.getName());
        } else if (methods.length == 0) {
            throw new ProxyCreationException("Channel interface must have one method: " + interfaze.getName());
        }

        EventStream stream = connection.getEventStream();
        Method method = methods[0];
        EventStreamHandler handler = stream.getHeadHandler();
        return new ChannelProxyObjectFactory<T>(uri, interfaze, method, handler, proxyFactory);
    }

    private <T> URI getClassLoaderUri(Class<T> interfaze) {
        if (!(interfaze.getClassLoader() instanceof MultiParentClassLoader)) {
            return Names.BOOT_CONTRIBUTION;
        }
        return ((MultiParentClassLoader) interfaze.getClassLoader()).getName();
    }

}
