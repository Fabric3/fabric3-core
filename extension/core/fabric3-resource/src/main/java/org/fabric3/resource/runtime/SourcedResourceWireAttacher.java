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
package org.fabric3.resource.runtime;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.resource.provision.SourcedWireTarget;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches to a service in the runtime domain.
 */
public class SourcedResourceWireAttacher implements TargetWireAttacher<SourcedWireTarget> {
    private ApplicationResourceRegistry resourceRegistry;
    private ComponentManager manager;

    public SourcedResourceWireAttacher(@Reference ApplicationResourceRegistry resourceRegistry, @Reference ComponentManager manager) {
        this.resourceRegistry = resourceRegistry;
        this.manager = manager;
    }

    public void attach(PhysicalWireSource source, SourcedWireTarget target, Wire wire) throws Fabric3Exception {
        throw new AssertionError();
    }

    public Supplier<?> createSupplier(SourcedWireTarget target) throws Fabric3Exception {
        String name = target.getUri().toString();
        Supplier supplier = resourceRegistry.getResourceFactory(name);
        if (supplier != null) {
            return supplier;
        }
        URI systemId = URI.create(Names.RUNTIME_NAME + "/" + name);
        AtomicComponent targetComponent = (AtomicComponent) manager.getComponent(systemId);
        if (targetComponent == null) {
            throw new Fabric3Exception("Resource not found: " + systemId);
        }
        return targetComponent.createSupplier();
    }
}
