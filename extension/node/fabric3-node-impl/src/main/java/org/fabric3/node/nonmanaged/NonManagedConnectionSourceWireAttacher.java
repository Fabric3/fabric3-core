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
 */
package org.fabric3.node.nonmanaged;

import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class NonManagedConnectionSourceWireAttacher implements SourceConnectionAttacher<NonManagedPhysicalConnectionSourceDefinition> {
    private ChannelProxyService proxyService;
    private ClassLoaderRegistry classLoaderRegistry;

    public NonManagedConnectionSourceWireAttacher(@Reference ChannelProxyService proxyService, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(NonManagedPhysicalConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        try {
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Class<?> interfaze = loader.loadClass(source.getInterface());
            Object proxy = proxyService.createObjectFactory(interfaze, connection).getInstance();
            source.setProxy(proxy);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    public void detach(NonManagedPhysicalConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {
    }
}
