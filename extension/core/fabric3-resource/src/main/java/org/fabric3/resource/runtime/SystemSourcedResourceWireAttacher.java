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

import org.fabric3.resource.provision.SystemSourcedWireTargetDefinition;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches to a service in the runtime domain.
 */
public class SystemSourcedResourceWireAttacher implements TargetWireAttacher<SystemSourcedWireTargetDefinition> {
    private final ComponentManager manager;

    public SystemSourcedResourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalWireSourceDefinition source, SystemSourcedWireTargetDefinition target, Wire wire) throws ContainerException {
        throw new AssertionError();
    }

    public void detach(PhysicalWireSourceDefinition source, SystemSourcedWireTargetDefinition target) throws ContainerException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(SystemSourcedWireTargetDefinition target) throws ContainerException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        AtomicComponent targetComponent = (AtomicComponent) manager.getComponent(targetId);
        if (targetComponent == null) {
            throw new ContainerException("Resource not found: " + targetId);
        }
        return targetComponent.createObjectFactory();
    }
}
