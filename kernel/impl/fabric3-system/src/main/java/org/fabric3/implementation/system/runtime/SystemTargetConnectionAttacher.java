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
package org.fabric3.implementation.system.runtime;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.component.InvokerEventStreamHandler;
import org.fabric3.implementation.system.provision.SystemConnectionTargetDefinition;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.util.UriHelper;

/**
 * Attaches and detaches a {@link ChannelConnection} from a System component consumer.
 */
@EagerInit
public class SystemTargetConnectionAttacher implements TargetConnectionAttacher<SystemConnectionTargetDefinition> {
    private ComponentManager manager;
    private ReflectionFactory reflectionFactory;
    private ClassLoaderRegistry classLoaderRegistry;

    public SystemTargetConnectionAttacher(@Reference ComponentManager manager,
                                          @Reference ReflectionFactory reflectionFactory,
                                          @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.reflectionFactory = reflectionFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalConnectionSourceDefinition source, SystemConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        URI targetUri = target.getUri();
        URI targetName = UriHelper.getDefragmentedName(targetUri);
        SystemComponent component = (SystemComponent) manager.getComponent(targetName);
        if (component == null) {
            throw new ContainerException("Target component not found: " + targetName);
        }
        ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());

        Method method = loadMethod(target, component);
        ConsumerInvoker invoker = reflectionFactory.createConsumerInvoker(method);

        InvokerEventStreamHandler handler = new InvokerEventStreamHandler(invoker, component, loader);
        EventStream stream = connection.getEventStream();
        stream.addHandler(handler);
    }

    public void detach(PhysicalConnectionSourceDefinition source, SystemConnectionTargetDefinition target) throws ContainerException {
        // no-op
    }

    private Method loadMethod(SystemConnectionTargetDefinition target, SystemComponent component) throws ContainerException {
        Signature signature = target.getConsumerSignature();
        Class<?> implementationClass = component.getImplementationClass();
        try {
            return signature.getMethod(implementationClass);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new ContainerException(e);
        }
    }

}