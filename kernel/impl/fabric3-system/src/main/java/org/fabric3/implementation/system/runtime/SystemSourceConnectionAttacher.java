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

import java.net.URI;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.system.provision.SystemConnectionSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches a {@link ChannelConnection} from a System component producer.
 */
@EagerInit
public class SystemSourceConnectionAttacher implements SourceConnectionAttacher<SystemConnectionSourceDefinition> {
    private ComponentManager manager;
    private ChannelProxyService proxyService;
    private ClassLoaderRegistry classLoaderRegistry;

    // loaded after bootstrap
    @Reference(required = false)
    public void setProxyService(List<ChannelProxyService> proxyServices) {
        proxyService = !proxyServices.isEmpty() ? proxyServices.get(0) : null;
    }

    public SystemSourceConnectionAttacher(@Reference ComponentManager manager, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(SystemConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws Fabric3Exception {
        if (proxyService == null) {
            throw new Fabric3Exception("Channel proxy service not found");
        }
        URI sourceUri = source.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceUri);
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        if (component == null) {
            throw new Fabric3Exception("Source component not found: " + sourceName);
        }
        Injectable injectable = source.getInjectable();
        Class<?> type = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
        ObjectFactory<?> factory = proxyService.createObjectFactory(type, connection);
        component.setObjectFactory(injectable, factory);
    }

    public void detach(SystemConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws Fabric3Exception {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeObjectFactory(injectable);
    }

}