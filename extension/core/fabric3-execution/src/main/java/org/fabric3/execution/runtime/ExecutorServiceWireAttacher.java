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
package org.fabric3.execution.runtime;

import java.util.concurrent.ExecutorService;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.execution.provision.ExecutorServiceWireTargetDefinition;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Wire;

/**
 */
public class ExecutorServiceWireAttacher implements TargetWireAttacher<ExecutorServiceWireTargetDefinition> {
    private SingletonObjectFactory<ExecutorService> factory;

    public ExecutorServiceWireAttacher(@Reference(name = "executorService") ExecutorService executorService) {
        ExecutorServiceProxy proxy = new ExecutorServiceProxy(executorService);
        this.factory = new SingletonObjectFactory<ExecutorService>(proxy);
    }

    public void attach(PhysicalWireSourceDefinition source, ExecutorServiceWireTargetDefinition target, Wire wire) throws ContainerException {
        throw new AssertionError();
    }

    public void detach(PhysicalWireSourceDefinition source, ExecutorServiceWireTargetDefinition target) throws ContainerException {
        throw new AssertionError();
    }

    public ObjectFactory<ExecutorService> createObjectFactory(ExecutorServiceWireTargetDefinition target) throws ContainerException {
        return factory;
    }

}