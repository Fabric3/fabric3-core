/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.pojo.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.builder.ChannelProxyService;
import org.fabric3.implementation.pojo.builder.ProxyCreationException;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * The default ChannelProxyService that uses JDK dynamic proxies.
 *
 * @version $$Rev$$ $$Date$$
 */
public class JDKChannelProxyService implements ChannelProxyService {
    private ClassLoaderRegistry classLoaderRegistry;

    public JDKChannelProxyService(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) throws ProxyCreationException {
        if (connection.getEventStreams().size() == 1) {
            return new OptimizedChannelConnectionObjectFactory<T>(interfaze, this, connection.getEventStreams().get(0));
        } else {
            Map<Method, EventStream> mappings = createInterfaceToStreamMapping(interfaze, connection);
            return new ChannelConnectionObjectFactory<T>(interfaze, this, mappings);
        }
    }

    public <T> T createProxy(Class<T> interfaze, Map<Method, EventStream> mappings) throws ProxyCreationException {
        ClassLoader loader = interfaze.getClassLoader();
        JDKEventHandler handler = new JDKEventHandler(mappings);
        return interfaze.cast(Proxy.newProxyInstance(loader, new Class[]{interfaze}, handler));
    }

    public <T> T createProxy(Class<T> interfaze, EventStream stream) throws ProxyCreationException {
        ClassLoader loader = interfaze.getClassLoader();
        OptimizedJDKEventHandler handler = new OptimizedJDKEventHandler(stream);
        return interfaze.cast(Proxy.newProxyInstance(loader, new Class[]{interfaze}, handler));
    }

    private Map<Method, EventStream> createInterfaceToStreamMapping(Class<?> interfaze, ChannelConnection connection) throws ProxyCreationException {
        List<EventStream> streams = connection.getEventStreams();
        Map<Method, EventStream> mappings = new HashMap<Method, EventStream>(streams.size());
        for (EventStream stream : streams) {
            PhysicalEventStreamDefinition definition = stream.getDefinition();
            try {
                Method method = findMethod(interfaze, definition);
                mappings.put(method, stream);
            } catch (NoSuchMethodException e) {
                throw new NoMethodForEventStreamException(definition.getName());
            } catch (ClassNotFoundException e) {
                throw new ProxyCreationException(e);
            }
        }
        return mappings;
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