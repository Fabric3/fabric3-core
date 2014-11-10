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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.wire.InvocationChainImpl;
import org.fabric3.fabric.container.wire.WireImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.Connector;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 * The default connector implementation.
 */
public class ConnectorImpl implements Connector {
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> interceptorBuilders;
    private Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers;
    private Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers;

    private ClassLoaderRegistry classLoaderRegistry;
    private TransformerInterceptorFactory transformerFactory;
    private boolean transform;

    /**
     * Constructor used during bootstrap
     */
    public ConnectorImpl() {
    }

    @Constructor
    public ConnectorImpl(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference TransformerInterceptorFactory transformerFactory) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.transformerFactory = transformerFactory;
        transform = true;
    }

    @Reference
    public void setInterceptorBuilders(Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> interceptorBuilders) {
        this.interceptorBuilders = interceptorBuilders;
    }

    @Reference(required = false)
    public void setSourceAttachers(Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>>
                                               sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference
    public void setTargetAttachers(Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>>
                                               targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    public void connect(PhysicalWireDefinition definition) throws ContainerException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = getAttacher(targetDefinition);
        if (targetAttacher == null) {
            throw new AttacherNotFoundException("Target attacher not found for type: " + targetDefinition.getClass());
        }

        if (definition.isOptimizable()) {
            ObjectFactory<?> objectFactory = targetAttacher.createObjectFactory(targetDefinition);
            sourceAttacher.attachObjectFactory(sourceDefinition, objectFactory, targetDefinition);
        } else {
            Wire wire = createWire(definition);
            sourceAttacher.attach(sourceDefinition, targetDefinition, wire);
            targetAttacher.attach(sourceDefinition, targetDefinition, wire);
        }
    }

    public void disconnect(PhysicalWireDefinition definition) throws ContainerException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }

        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        if (definition.isOptimizable()) {
            sourceAttacher.detachObjectFactory(sourceDefinition, targetDefinition);
        } else {
            TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = getAttacher(targetDefinition);
            if (targetAttacher == null) {
                throw new AttacherNotFoundException("Target attacher not found for type: " + targetDefinition.getClass());
            }
            targetAttacher.detach(sourceDefinition, targetDefinition);
            sourceAttacher.detach(sourceDefinition, targetDefinition);
        }
    }

    Wire createWire(PhysicalWireDefinition definition) throws ContainerException {
        Wire wire = new WireImpl();
        for (PhysicalOperationDefinition operation : definition.getOperations()) {
            InvocationChain chain = new InvocationChainImpl(operation);
            for (PhysicalInterceptorDefinition interceptorDefinition : operation.getInterceptors()) {
                InterceptorBuilder<? super PhysicalInterceptorDefinition> builder = getBuilder(interceptorDefinition);
                Interceptor interceptor = builder.build(interceptorDefinition);
                chain.addInterceptor(interceptor);
            }
            wire.addInvocationChain(chain);
        }
        processTransform(wire, definition);
        return wire;
    }

    /**
     * Handles adding required parameter data transformers to a wire.
     *
     * @param wire       the wire
     * @param definition the physical wire definition
     * @throws ContainerException if there is an error creating a transformer
     */
    private void processTransform(Wire wire, PhysicalWireDefinition definition) throws ContainerException {
        if (!transform) {
            // short-circuit during bootstrap
            return;
        }
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        for (DataType sourceType : sourceDefinition.getDataTypes()) {
            if (targetDefinition.getDataTypes().contains(sourceType)) {
                // transform for pass-by-value and not for different datatypes.
                addTransformer(wire, definition, true);
                return;
            }
        }
        addTransformer(wire, definition, false);
    }

    /**
     * Adds a transformer if parameter data needs to be copied from one format to another or pass-by-value semantics must be enforced.
     *
     * @param wire           the wire
     * @param definition     the physical wire definition
     * @param checkPassByRef true if a check needs to be performed for support of pass-by-reference
     * @throws ContainerException if there is an error creating a transformer
     */
    private void addTransformer(Wire wire, PhysicalWireDefinition definition, boolean checkPassByRef) throws ContainerException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        URI sourceId = sourceDefinition.getClassLoaderId();
        URI targetId = targetDefinition.getClassLoaderId();
        ClassLoader sourceLoader = null;
        ClassLoader targetLoader = null;
        for (InvocationChain chain : wire.getInvocationChains()) {
            if (checkPassByRef && chain.getPhysicalOperation().isAllowsPassByReference()) {
                continue;
            }
            // lazy load classloaders
            if (sourceLoader == null && targetLoader == null) {
                sourceLoader = classLoaderRegistry.getClassLoader(sourceId);
                targetLoader = classLoaderRegistry.getClassLoader(targetId);
            }
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            List<DataType> sourceTypes = sourceDefinition.getDataTypes();
            List<DataType> targetTypes = targetDefinition.getDataTypes();
            Interceptor interceptor = transformerFactory.createInterceptor(operation, sourceTypes, targetTypes, targetLoader, sourceLoader);
            chain.addInterceptor(interceptor);
        }
    }

    @SuppressWarnings("unchecked")
    private <PID extends PhysicalInterceptorDefinition> InterceptorBuilder<PID> getBuilder(PID definition) {
        return (InterceptorBuilder<PID>) interceptorBuilders.get(definition.getClass());

    }

    @SuppressWarnings("unchecked")
    private <PSD extends PhysicalWireSourceDefinition> SourceWireAttacher<PSD> getAttacher(PSD source) {
        return (SourceWireAttacher<PSD>) sourceAttachers.get(source.getClass());
    }

    @SuppressWarnings("unchecked")
    private <PSD extends PhysicalWireTargetDefinition> TargetWireAttacher<PSD> getAttacher(PSD target) {
        return (TargetWireAttacher<PSD>) targetAttachers.get(target.getClass());
    }
}
