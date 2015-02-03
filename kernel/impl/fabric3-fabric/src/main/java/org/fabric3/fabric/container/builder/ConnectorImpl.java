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
package org.fabric3.fabric.container.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.fabric.container.wire.InvocationChainImpl;
import org.fabric3.fabric.container.wire.WireImpl;
import org.fabric3.spi.container.builder.Connector;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.util.Cast;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 * The default connector implementation.
 */
public class ConnectorImpl implements Connector {
    @Reference(required = false)
    protected Map<Class<?>, InterceptorBuilder<?>> interceptorBuilders = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, SourceWireAttacher<?>> sourceAttachers = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, TargetWireAttacher<?>> targetAttachers = new HashMap<>();

    @Constructor
    public ConnectorImpl() {
    }

    public ConnectorImpl(Map<Class<?>, SourceWireAttacher<?>> sourceAttachers, Map<Class<?>, TargetWireAttacher<?>> targetAttachers) {
        this.sourceAttachers = sourceAttachers;
        this.targetAttachers = targetAttachers;
    }

    public void connect(PhysicalWireDefinition definition) throws Fabric3Exception {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = Cast.cast(sourceAttachers.get(sourceDefinition.getClass()));
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Source attacher not found for type: " + sourceDefinition.getClass());
        }
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = Cast.cast(targetAttachers.get(targetDefinition.getClass()));
        if (targetAttacher == null) {
            throw new Fabric3Exception("Target attacher not found for type: " + targetDefinition.getClass());
        }

        if (definition.isOptimizable()) {
            Supplier<?> supplier = targetAttacher.createSupplier(targetDefinition);
            sourceAttacher.attachSupplier(sourceDefinition, supplier, targetDefinition);
        } else {
            Wire wire = createWire(definition);
            sourceAttacher.attach(sourceDefinition, targetDefinition, wire);
            targetAttacher.attach(sourceDefinition, targetDefinition, wire);
        }
    }

    public void disconnect(PhysicalWireDefinition definition) throws Fabric3Exception {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = Cast.cast(sourceAttachers.get(sourceDefinition.getClass()));
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Source attacher not found for type: " + sourceDefinition.getClass());
        }

        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        if (definition.isOptimizable()) {
            sourceAttacher.detachSupplier(sourceDefinition, targetDefinition);
        } else {
            TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = Cast.cast(targetAttachers.get(targetDefinition.getClass()));
            if (targetAttacher == null) {
                throw new Fabric3Exception("Target attacher not found for type: " + targetDefinition.getClass());
            }
            targetAttacher.detach(sourceDefinition, targetDefinition);
            sourceAttacher.detach(sourceDefinition, targetDefinition);
        }
    }

    Wire createWire(PhysicalWireDefinition definition) throws Fabric3Exception {
        Wire wire = new WireImpl();
        for (PhysicalOperationDefinition operation : definition.getOperations()) {
            InvocationChain chain = new InvocationChainImpl(operation);
            for (PhysicalInterceptorDefinition interceptorDefinition : operation.getInterceptors()) {
                InterceptorBuilder<? super PhysicalInterceptorDefinition> builder = Cast.cast(interceptorBuilders.get(interceptorDefinition.getClass()));
                Interceptor interceptor = builder.build(interceptorDefinition);
                chain.addInterceptor(interceptor);
            }
            wire.addInvocationChain(chain);
        }
        return wire;
    }

}
