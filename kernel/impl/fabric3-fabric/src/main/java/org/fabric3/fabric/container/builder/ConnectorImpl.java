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
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.builder.InterceptorBuilder;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalInterceptor;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWire;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
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

    public void connect(PhysicalWire physicalWire) throws Fabric3Exception {
        PhysicalWireSource source = physicalWire.getSource();
        SourceWireAttacher<PhysicalWireSource> sourceAttacher = Cast.cast(sourceAttachers.get(source.getClass()));
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Source attacher not found for type: " + source.getClass());
        }
        PhysicalWireTarget target = physicalWire.getTarget();
        TargetWireAttacher<PhysicalWireTarget> targetAttacher = Cast.cast(targetAttachers.get(target.getClass()));
        if (targetAttacher == null) {
            throw new Fabric3Exception("Target attacher not found for type: " + target.getClass());
        }

        if (physicalWire.isOptimizable()) {
            Supplier<?> supplier = targetAttacher.createSupplier(target);
            sourceAttacher.attachSupplier(source, supplier, target);
        } else {
            Wire wire = createWire(physicalWire);
            sourceAttacher.attach(source, target, wire);
            targetAttacher.attach(source, target, wire);
        }
    }

    public void disconnect(PhysicalWire physicalWire) throws Fabric3Exception {
        PhysicalWireSource source = physicalWire.getSource();
        SourceWireAttacher<PhysicalWireSource> sourceAttacher = Cast.cast(sourceAttachers.get(source.getClass()));
        if (sourceAttacher == null) {
            throw new Fabric3Exception("Source attacher not found for type: " + source.getClass());
        }

        PhysicalWireTarget target = physicalWire.getTarget();
        if (physicalWire.isOptimizable()) {
            sourceAttacher.detachSupplier(source, target);
        } else {
            TargetWireAttacher<PhysicalWireTarget> targetAttacher = Cast.cast(targetAttachers.get(target.getClass()));
            if (targetAttacher == null) {
                throw new Fabric3Exception("Target attacher not found for type: " + target.getClass());
            }
            targetAttacher.detach(source, target);
            sourceAttacher.detach(source, target);
        }
    }

    Wire createWire(PhysicalWire physicalWire) throws Fabric3Exception {
        Wire wire = new WireImpl();
        for (PhysicalOperation operation : physicalWire.getOperations()) {
            InvocationChain chain = new InvocationChainImpl(operation);
            for (PhysicalInterceptor physicalInterceptor : operation.getInterceptors()) {
                InterceptorBuilder<? super PhysicalInterceptor> builder = Cast.cast(interceptorBuilders.get(physicalInterceptor.getClass()));
                Interceptor interceptor = builder.build(physicalInterceptor);
                chain.addInterceptor(interceptor);
            }
            wire.addInvocationChain(chain);
        }
        return wire;
    }

}
