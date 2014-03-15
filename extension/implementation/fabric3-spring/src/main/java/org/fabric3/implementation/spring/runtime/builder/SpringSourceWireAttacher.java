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
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.spi.container.builder.BuildException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.spring.provision.SpringWireSourceDefinition;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spring.spi.WireListener;

/**
 * Attaches the source side of a wire to a Spring component.
 */
@EagerInit
public class SpringSourceWireAttacher implements SourceWireAttacher<SpringWireSourceDefinition> {
    private ComponentManager manager;
    private WireProxyService proxyService;

    private ClassLoaderRegistry classLoaderRegistry;
    private List<WireListener> listeners = Collections.emptyList();

    public SpringSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference WireProxyService proxyService,
                                    @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
        this.proxyService = proxyService;
    }

    @Reference(required = false)
    public void setListeners(List<WireListener> listeners) {
        this.listeners = listeners;
    }

    public void attach(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws BuildException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(source.getInterface());
            // note callbacks not supported for spring beans
            ObjectFactory<?> factory = proxyService.createObjectFactory(interfaze, wire, null);
            component.attach(referenceName, interfaze, factory);
            for (WireListener listener : listeners) {
                listener.onAttach(wire);
            }
        } catch (ClassNotFoundException | ProxyCreationException e) {
            throw new BuildException(e);
        }
    }

    public void attachObjectFactory(SpringWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws BuildException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(source.getInterface());
            component.attach(referenceName, interfaze, objectFactory);
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        }
    }

    public void detach(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuildException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        component.detach(referenceName);
    }

    public void detachObjectFactory(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuildException {
        detach(source, target);
    }

    private SpringComponent getComponent(SpringWireSourceDefinition definition) throws BuildException {
        URI uri = definition.getUri();
        SpringComponent component = (SpringComponent) manager.getComponent(uri);
        if (component == null) {
            throw new BuildException("Source not found: " + uri);
        }
        return component;
    }


}
