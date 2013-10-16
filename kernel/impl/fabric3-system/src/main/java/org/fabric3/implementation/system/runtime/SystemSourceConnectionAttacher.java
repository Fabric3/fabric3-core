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

import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.system.provision.SystemConnectionSourceDefinition;
import org.fabric3.spi.container.builder.component.ConnectionAttachException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import java.net.URI;
import java.util.List;

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
            throws ConnectionAttachException {
        if (proxyService == null) {
            throw new ConnectionAttachException("Channel proxy service not found");
        }
        URI sourceUri = source.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceUri);
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        if (component == null) {
            throw new ConnectionAttachException("Source component not found: " + sourceName);
        }
        Injectable injectable = source.getInjectable();
        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = source.getInterfaceName();
            throw new ConnectionAttachException("Unable to load interface class: " + name, e);
        }
        try {
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, connection);
            component.setObjectFactory(injectable, factory);
        } catch (ProxyCreationException e) {
            throw new ConnectionAttachException(e);
        }
    }

    public void detach(SystemConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ConnectionAttachException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeObjectFactory(injectable);
    }

}