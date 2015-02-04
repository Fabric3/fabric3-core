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
package org.fabric3.implementation.java.runtime;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

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

    public void attach(JavaWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire) {
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent source = (JavaComponent) manager.getComponent(sourceName);
        if (source == null) {
            throw new Fabric3Exception("Source callback not found: " + sourceName);
        }
        Injectable injectable = sourceDefinition.getInjectable();

        Class<?> type = sourceDefinition.getInterfaceClass();
        if (InjectableType.CALLBACK.equals(injectable.getType())) {
            processCallback(wire, targetDefinition, source, injectable, type);
        } else {
            processReference(wire, sourceDefinition, targetDefinition, source, injectable, type);
        }
    }

    public void detach(JavaWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        detachSupplier(source, target);
    }

    public void detachSupplier(JavaWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent component = (JavaComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeSupplier(injectable);
    }

    public void attachSupplier(JavaWireSourceDefinition sourceDefinition, Supplier<?> supplier, PhysicalWireTargetDefinition targetDefinition) {
        URI sourceId = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent sourceComponent = (JavaComponent) manager.getComponent(sourceId);
        Injectable injectable = sourceDefinition.getInjectable();

        if (sourceDefinition.isKeyed() || sourceDefinition.isOrdered()) {
            Object key = getKey(sourceDefinition, targetDefinition);
            int order = sourceDefinition.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            sourceComponent.setSupplier(injectable, supplier, attributes);
        } else {
            sourceComponent.setSupplier(injectable, supplier);
        }
    }

    private void processReference(Wire wire,
                                  JavaWireSourceDefinition sourceDefinition,
                                  PhysicalWireTargetDefinition targetDefinition,
                                  JavaComponent source,
                                  Injectable injectable,
                                  Class<?> type) {
        String callbackUri = null;
        URI uri = targetDefinition.getCallbackUri();
        if (uri != null) {
            callbackUri = uri.toString();
        }

        Supplier<?> supplier = proxyService.createSupplier(type, wire, callbackUri);
        if (sourceDefinition.isKeyed() || sourceDefinition.isOrdered()) {
            Object key = getKey(sourceDefinition, targetDefinition);
            int order = sourceDefinition.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            source.setSupplier(injectable, supplier, attributes);
        } else {
            source.setSupplier(injectable, supplier);
        }
    }

    private void processCallback(Wire wire, PhysicalWireTargetDefinition targetDefinition, JavaComponent source, Injectable injectable, Class<?> type) {
        URI callbackUri = targetDefinition.getUri();
        ScopeContainer container = source.getScopeContainer();
        Supplier<?> supplier = source.getSupplier(injectable);
        boolean multiThreaded = Scope.COMPOSITE.equals(container.getScope());
        if (supplier == null) {
            supplier = proxyService.createCallbackSupplier(type, multiThreaded, callbackUri, wire);
        } else {
            supplier = proxyService.updateCallbackSupplier(supplier, type, multiThreaded, callbackUri, wire);
        }
        source.setSupplier(injectable, supplier);
    }

}
