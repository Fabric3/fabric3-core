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
import java.util.List;

import org.fabric3.host.Names;
import org.fabric3.implementation.bytecode.proxy.common.MethodSorter;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactory;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyServiceExtension;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that delegates to a {@link ProxyFactory} to create channel proxies.
 */
public class BytecodeChannelProxyService implements ChannelProxyServiceExtension {
    private ProxyFactory proxyFactory;
    private ClassLoaderRegistry classLoaderRegistry;

    public BytecodeChannelProxyService(@Reference ProxyFactory proxyFactory, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.proxyFactory = proxyFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public boolean isDefault() {
        return false;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) throws ProxyCreationException {
        URI uri = getClassLoaderUri(interfaze);

        Method[] methods = MethodSorter.sort(interfaze.getMethods());
        EventStreamHandler[] handlers = new EventStreamHandler[methods.length];

        try {
            for (EventStream eventStream : connection.getEventStreams()) {
                Method method = findMethod(interfaze, eventStream.getDefinition());
                for (int i = 0; i < methods.length; i++) {
                    if (method.equals(methods[i])) {
                        handlers[i] = eventStream.getHeadHandler();
                        break;
                    }
                }
            }
            return new ChannelProxyObjectFactory<T>(uri, interfaze, methods, handlers, proxyFactory);
        } catch (ClassNotFoundException e) {
            throw new ProxyCreationException(e);
        } catch (NoSuchMethodException e) {
            throw new ProxyCreationException(e);
        }
    }

    private <T> URI getClassLoaderUri(Class<T> interfaze) {
        if (!(interfaze.getClassLoader() instanceof MultiParentClassLoader)) {
            return Names.BOOT_CONTRIBUTION;
        }
        return ((MultiParentClassLoader) interfaze.getClassLoader()).getName();
    }

    /**
     * Returns the matching method from the class for a given operation.
     *
     * @param clazz      the class to introspect
     * @param definition the event stream to match
     * @return a matching method
     * @throws NoSuchMethodException  if a matching method is not found
     * @throws ClassNotFoundException if a parameter type specified in the operation is not found
     */
    private Method findMethod(Class<?> clazz, PhysicalEventStreamDefinition definition) throws NoSuchMethodException, ClassNotFoundException {
        String name = definition.getName();
        List<String> eventTypes = definition.getEventTypes();
        Class<?>[] types = new Class<?>[eventTypes.size()];
        for (int i = 0; i < eventTypes.size(); i++) {
            types[i] = classLoaderRegistry.loadClass(clazz.getClassLoader(), eventTypes.get(i));
        }
        return clazz.getMethod(name, types);
    }

}
