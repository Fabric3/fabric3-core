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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.builder.MethodUtils;
import org.fabric3.implementation.spring.provision.SpringTargetDefinition;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.implementation.spring.runtime.component.SpringInvoker;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spring.spi.WireListener;

/**
 * Attaches the target side of a wire to a Spring component.
 */
@EagerInit
public class SpringTargetWireAttacher implements TargetWireAttacher<SpringTargetDefinition> {
    private ComponentManager manager;
    private ClassLoaderRegistry classLoaderRegistry;
    private List<WireListener> listeners = Collections.emptyList();

    public SpringTargetWireAttacher(@Reference ComponentManager manager, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference(required = false)
    public void setListeners(List<WireListener> listeners) {
        this.listeners = listeners;
    }

    public void attach(PhysicalSourceDefinition source, SpringTargetDefinition target, Wire wire) throws WiringException {
        String beanName = target.getBeanName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(target.getBeanInterface());
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
        for (WireListener listener : listeners) {
            listener.onAttach(wire);
        }
        SpringComponent component = getComponent(target);
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            Method beanMethod = MethodUtils.findMethod(source, target, operation, interfaze, loader, classLoaderRegistry);
            SpringInvoker invoker = new SpringInvoker(beanName, beanMethod, component);
            chain.addInterceptor(invoker);
        }
    }

    public void detach(PhysicalSourceDefinition source, SpringTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(SpringTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private SpringComponent getComponent(SpringTargetDefinition definition) throws WiringException {
        URI uri = UriHelper.getDefragmentedName(definition.getUri());
        SpringComponent component = (SpringComponent) manager.getComponent(uri);
        if (component == null) {
            throw new WiringException("Target not found: " + uri);
        }
        return component;
    }

}
