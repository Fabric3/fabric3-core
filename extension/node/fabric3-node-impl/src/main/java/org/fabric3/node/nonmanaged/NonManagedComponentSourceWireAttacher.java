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
package org.fabric3.node.nonmanaged;

import org.fabric3.api.host.Names;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a wire or object factory to non-managed code. The attachment is done by generating a proxy or returning an object instance which is then set on the
 * physical source definition. The proxy or instance can then be returned to the non-managed code.
 */
@EagerInit
public class NonManagedComponentSourceWireAttacher implements SourceWireAttacher<NonManagedPhysicalSourceDefinition> {
    private WireProxyService proxyService;
    private ClassLoaderRegistry classLoaderRegistry;

    public NonManagedComponentSourceWireAttacher(@Reference WireProxyService proxyService, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(NonManagedPhysicalSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        try {
            ClassLoader loader = classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION);
            Class<?> interfaze = loader.loadClass(source.getInterface());
            Object proxy = proxyService.createObjectFactory(interfaze, wire, null).getInstance();
            source.setProxy(proxy);
        } catch (ProxyCreationException e) {
            throw new WiringException(e);
        } catch (ObjectCreationException e) {
            throw new WiringException(e);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
    }

    public void attachObjectFactory(NonManagedPhysicalSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        try {
            source.setProxy(objectFactory.getInstance());
        } catch (ObjectCreationException e) {
            throw new WiringException(e);
        }
    }

    public void detach(NonManagedPhysicalSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
    }

    public void detachObjectFactory(NonManagedPhysicalSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
    }
}
