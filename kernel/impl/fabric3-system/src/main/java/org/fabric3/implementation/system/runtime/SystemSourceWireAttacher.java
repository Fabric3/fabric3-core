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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.runtime;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.system.provision.SystemWireSourceDefinition;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class SystemSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<SystemWireSourceDefinition> {

    private final ComponentManager manager;
    private WireProxyService proxyService;

    public SystemSourceWireAttacher(@Reference ComponentManager manager, @Reference TransformerRegistry transformerRegistry) {
        super(transformerRegistry);
        this.manager = manager;
    }

    /**
     * Used for lazy injection of the proxy service. Since the ProxyService is only available after extensions are loaded and this class is loaded during
     * runtime bootstrap, injection of the former service must be delayed. This is achieved by setting the reference to no required. when the ProxyService
     * becomes available, it will be wired to this reference.
     *
     * @param proxyService the service used to create reference proxies
     */
    @Reference(required = false)
    public void setProxyService(WireProxyService proxyService) {
        this.proxyService = proxyService;
    }

    public void attach(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) {
        if (proxyService == null) {
            throw new Fabric3Exception("Attempt to inject a non-optimized wire during runtime bootstrap.");
        }
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();

        Class<?> type = source.getInterfaceClass();
        if (InjectableType.CALLBACK.equals(injectable.getType())) {
            throw new UnsupportedOperationException("Callbacks are not supported on system components");
        } else {
            String callbackUri = null;
            URI uri = target.getCallbackUri();
            if (uri != null) {
                callbackUri = uri.toString();
            }
            Supplier<?> factory = proxyService.createSupplier(type, wire, callbackUri);

            if (source.isKeyed() || source.isOrdered()) {
                Object key = getKey(source, target);
                int order = source.getOrder();
                InjectionAttributes attributes = new InjectionAttributes(key, order);
                component.setSupplier(injectable, factory, attributes);
            } else {
                component.setSupplier(injectable, factory);
            }
        }
    }

    public void detach(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        detachSupplier(source, target);
    }

    public void detachSupplier(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent component = (SystemComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeSupplier(injectable);
    }

    public void attachSupplier(SystemWireSourceDefinition source, Supplier<?> supplier, PhysicalWireTargetDefinition target) {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent component = (SystemComponent) manager.getComponent(sourceId);
        Injectable injectable = source.getInjectable();
        if (source.isKeyed() || source.isOrdered()) {
            Object key = getKey(source, target);
            int order = source.getOrder();
            InjectionAttributes attributes = new InjectionAttributes(key, order);
            component.setSupplier(injectable, supplier, attributes);
        } else {
            component.setSupplier(injectable, supplier);
        }
    }
}