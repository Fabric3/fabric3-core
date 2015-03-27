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

import java.util.function.Supplier;

import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a wire or Supplier to non-managed code. The attachment is done by generating a proxy or returning an object instance which is then set on the
 * physical source definition. The proxy or instance can then be returned to the non-managed code.
 */
@EagerInit
public class NonManagedComponentSourceWireAttacher implements SourceWireAttacher<NonManagedWireSource> {
    private WireProxyService proxyService;

    public NonManagedComponentSourceWireAttacher(@Reference WireProxyService proxyService) {
        this.proxyService = proxyService;
    }

    public void attach(NonManagedWireSource source, PhysicalWireTarget target, Wire wire) {
        Class<?> interfaze = source.getInterface();
        Object proxy = proxyService.createSupplier(interfaze, wire, null).get();
        source.setProxy(proxy);
    }

    public void attachSupplier(NonManagedWireSource source, Supplier<?> supplier, PhysicalWireTarget target) {
        source.setProxy(supplier.get());
    }

    public void detach(NonManagedWireSource source, PhysicalWireTarget target) {
    }

    public void detachSupplier(NonManagedWireSource source, PhysicalWireTarget target) {
        // no-op
    }
}
