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
package org.fabric3.implementation.system.singleton;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Reinjects singleton components after the runtime bootstrap.
 */
@EagerInit
public class SingletonSourceWireAttacher implements SourceWireAttacher<SingletonWireSource> {
    private ComponentManager manager;

    public SingletonSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(SingletonWireSource source, PhysicalWireTarget target, Wire wire) {
        throw new UnsupportedOperationException();
    }

    public void detach(SingletonWireSource source, PhysicalWireTarget target) {
    }

    public void detachSupplier(SingletonWireSource source, PhysicalWireTarget target) {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SingletonComponent component = (SingletonComponent) manager.getComponent(sourceName);
        Injectable injectable = source.getInjectable();
        component.removeSupplier(injectable);
    }

    public void attachSupplier(SingletonWireSource source, Supplier<?> supplier, PhysicalWireTarget target) {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SingletonComponent sourceComponent = (SingletonComponent) manager.getComponent(sourceId);
        Injectable injectable = source.getInjectable();
        // Add the Supplier for the target to be reinjected.
        // The Injectable identifies the injection site (a field or method) on the singleton instance.
        String key = source.getKey();
        int order = source.getOrder();
        InjectionAttributes attributes = new InjectionAttributes(key, order);
        sourceComponent.addSupplier(injectable, supplier, attributes);
    }
}