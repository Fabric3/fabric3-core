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
package org.fabric3.implementation.java.runtime;

import java.net.URI;

import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.builder.KeyInstantiationException;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches and detaches wires from a Java component.
 */
@EagerInit
public class JavaSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JavaWireSourceDefinition> {

    private ComponentManager manager;
    private WireProxyService proxyService;

    public JavaSourceWireAttacher(@Reference ComponentManager manager,
                                  @Reference WireProxyService proxyService,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference TransformerRegistry transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
    }

    public void attach(JavaWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire) throws WiringException {
        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent source = (JavaComponent) manager.getComponent(sourceName);
        if (source == null) {
            throw new WiringException("Source callback not found: " + sourceName);
        }
        Injectable injectable = sourceDefinition.getInjectable();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class: " + name, sourceUri, null, e);
        }
        if (InjectableType.CALLBACK.equals(injectable.getType())) {
            processCallback(wire, targetDefinition, source, injectable, type);
        } else {
            processReference(wire, sourceDefinition, targetDefinition, source, injectable, type);
        }
    }

    public void detach(JavaWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(JavaWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent component = (JavaComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeObjectFactory(injectable);
    }

    public void attachObjectFactory(JavaWireSourceDefinition sourceDefinition, ObjectFactory<?> factory, PhysicalWireTargetDefinition targetDefinition)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent sourceComponent = (JavaComponent) manager.getComponent(sourceId);
        Injectable injectable = sourceDefinition.getInjectable();

        if (sourceDefinition.isKeyed() || sourceDefinition.isOrdered()) {
            Object key = getKey(sourceDefinition, targetDefinition);
            int order = sourceDefinition.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            sourceComponent.setObjectFactory(injectable, factory, attributes);
        } else {
            sourceComponent.setObjectFactory(injectable, factory);
        }
    }

    private void processReference(Wire wire,
                                  JavaWireSourceDefinition sourceDefinition,
                                  PhysicalWireTargetDefinition targetDefinition,
                                  JavaComponent source,
                                  Injectable injectable,
                                  Class<?> type) throws KeyInstantiationException {
        String callbackUri = null;
        URI uri = targetDefinition.getCallbackUri();
        if (uri != null) {
            callbackUri = uri.toString();
        }

        try {
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, wire, callbackUri);
            if (sourceDefinition.isKeyed() || sourceDefinition.isOrdered()) {
                Object key = getKey(sourceDefinition, targetDefinition);
                int order = sourceDefinition.getOrder();
                InjectionAttributes attributes = new InjectionAttributes(key, order);
                source.setObjectFactory(injectable, factory, attributes);
            } else {
                source.setObjectFactory(injectable, factory);
            }
        } catch (ProxyCreationException e) {
            throw new KeyInstantiationException(e);
        }
    }

    private void processCallback(Wire wire, PhysicalWireTargetDefinition targetDefinition, JavaComponent source, Injectable injectable, Class<?> type)
            throws KeyInstantiationException {
        URI callbackUri = targetDefinition.getUri();
        ScopeContainer container = source.getScopeContainer();
        ObjectFactory<?> factory = source.getObjectFactory(injectable);
        boolean multiThreaded = Scope.COMPOSITE.equals(container.getScope());
        try {
            if (factory == null) {
                factory = proxyService.createCallbackObjectFactory(type, multiThreaded, callbackUri, wire);
            } else {
                factory = proxyService.updateCallbackObjectFactory(factory, type, multiThreaded, callbackUri, wire);
            }
            source.setObjectFactory(injectable, factory);
        } catch (ProxyCreationException e) {
            throw new KeyInstantiationException(e);
        }
    }

}
