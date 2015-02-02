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
package org.fabric3.implementation.system.runtime;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.component.InvokerEventStreamHandler;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.system.provision.SystemConnectionTargetDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

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
            throws Fabric3Exception {
        URI targetUri = target.getUri();
        URI targetName = UriHelper.getDefragmentedName(targetUri);
        SystemComponent component = (SystemComponent) manager.getComponent(targetName);
        if (component == null) {
            throw new Fabric3Exception("Target component not found: " + targetName);
        }
        ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());

        Method method = target.getConsumerMethod();
        ConsumerInvoker invoker = reflectionFactory.createConsumerInvoker(method);

        InvokerEventStreamHandler handler = new InvokerEventStreamHandler(invoker, component, loader);
        EventStream stream = connection.getEventStream();
        stream.addHandler(handler);
    }

    public void detach(PhysicalConnectionSourceDefinition source, SystemConnectionTargetDefinition target) throws Fabric3Exception {
        // no-op
    }

}