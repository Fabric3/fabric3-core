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
package org.fabric3.implementation.system.singleton;

import java.net.URI;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Exists as a no-op attacher for system singleton components
 */
@EagerInit
public class SingletonTargetWireAttacher implements TargetWireAttacher<SingletonWireTargetDefinition> {
    private final ComponentManager manager;

    public SingletonTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target, Wire wire)
            throws ContainerException {
    }

    public void detach(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target) throws ContainerException {
    }

    public ObjectFactory<?> createObjectFactory(SingletonWireTargetDefinition target) throws ContainerException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SingletonComponent targetComponent = (SingletonComponent) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
