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
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches wires from a Java component.
 */
@EagerInit
public class JavaSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JavaWireSource> {

    private ComponentManager manager;
    private WireProxyService proxyService;

    public JavaSourceWireAttacher(@Reference ComponentManager manager,
                                  @Reference WireProxyService proxyService,
                                  @Reference TransformerRegistry transformerRegistry) {
        super(transformerRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
    }

    public void attach(JavaWireSource source, PhysicalWireTarget target, Wire wire) {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent component = (JavaComponent) manager.getComponent(sourceName);
        if (component == null) {
            throw new Fabric3Exception("Source callback not found: " + sourceName);
        }
        Injectable injectable = source.getInjectable();

        Class<?> type = source.getInterfaceClass();
        if (InjectableType.CALLBACK.equals(injectable.getType())) {
            processCallback(wire, target, component, injectable, type);
        } else {
            processReference(wire, source, target, component, injectable, type);
        }
    }

    public void detach(JavaWireSource source, PhysicalWireTarget target) {
        detachSupplier(source, target);
    }

    public void detachSupplier(JavaWireSource source, PhysicalWireTarget target) {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent component = (JavaComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeSupplier(injectable);
    }

    public void attachSupplier(JavaWireSource source, Supplier<?> supplier, PhysicalWireTarget targetDefinition) {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent sourceComponent = (JavaComponent) manager.getComponent(sourceId);
        Injectable injectable = source.getInjectable();

        if (source.isKeyed() || source.isOrdered()) {
            Object key = getKey(source, targetDefinition);
            int order = source.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            sourceComponent.setSupplier(injectable, supplier, attributes);
        } else {
            sourceComponent.setSupplier(injectable, supplier);
        }
    }

    private void processReference(Wire wire, JavaWireSource source, PhysicalWireTarget target, JavaComponent component, Injectable injectable, Class<?> type) {
        String callbackUri = null;
        URI uri = target.getCallbackUri();
        if (uri != null) {
            callbackUri = uri.toString();
        }

        Supplier<?> supplier = proxyService.createSupplier(type, wire, callbackUri);
        if (source.isKeyed() || source.isOrdered()) {
            Object key = getKey(source, target);
            int order = source.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            component.setSupplier(injectable, supplier, attributes);
        } else {
            component.setSupplier(injectable, supplier);
        }
    }

    private void processCallback(Wire wire, PhysicalWireTarget targetDefinition, JavaComponent source, Injectable injectable, Class<?> type) {
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
