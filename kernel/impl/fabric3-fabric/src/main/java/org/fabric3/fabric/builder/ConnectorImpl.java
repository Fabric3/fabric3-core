/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.builder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.builder.transform.TransformerInterceptorFactory;
import org.fabric3.fabric.wire.InvocationChainImpl;
import org.fabric3.fabric.wire.WireImpl;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The default connector implmentation.
 *
 * @version $$Rev$$ $$Date$$
 */
public class ConnectorImpl implements Connector {
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> interceptorBuilders;
    private Map<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>> sourceAttachers;
    private Map<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>> targetAttachers;

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
    public void setSourceAttachers(Map<Class<? extends PhysicalSourceDefinition>,
            SourceWireAttacher<? extends PhysicalSourceDefinition>> sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference
    public void setTargetAttachers(Map<Class<? extends PhysicalTargetDefinition>,
            TargetWireAttacher<? extends PhysicalTargetDefinition>> targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    public void connect(PhysicalWireDefinition definition) throws BuilderException {
        PhysicalSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }
        PhysicalTargetDefinition targetDefinition = definition.getTarget();
        TargetWireAttacher<PhysicalTargetDefinition> targetAttacher = getAttacher(targetDefinition);
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

    public void disconnect(PhysicalWireDefinition definition) throws BuilderException {
        PhysicalSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }

        PhysicalTargetDefinition targetDefinition = definition.getTarget();
        if (definition.isOptimizable()) {
            sourceAttacher.detachObjectFactory(sourceDefinition, targetDefinition);
        } else {
            TargetWireAttacher<PhysicalTargetDefinition> targetAttacher = getAttacher(targetDefinition);
            if (targetAttacher == null) {
                throw new AttacherNotFoundException("Target attacher not found for type: " + targetDefinition.getClass());
            }
            targetAttacher.detach(sourceDefinition, targetDefinition);
            sourceAttacher.detach(sourceDefinition, targetDefinition);
        }
    }

    Wire createWire(PhysicalWireDefinition definition) throws BuilderException {
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

    private void processTransform(Wire wire, PhysicalWireDefinition definition) throws WiringException {
        if (!transform) {
            // short-circuit during bootstrap
            return;
        }
        PhysicalSourceDefinition sourceDefinition = definition.getSource();
        PhysicalTargetDefinition targetDefinition = definition.getTarget();
        for (DataType<?> sourceType : sourceDefinition.getPhysicalDataTypes()) {
            if (targetDefinition.getPhysicalDataTypes().contains(sourceType)) {
                // no transform necessary
                // TODO record source and target types on physical operation definition
                return;
            }
        }
        URI targetId = targetDefinition.getClassLoaderId();
        ClassLoader targetLoader = classLoaderRegistry.getClassLoader(targetId);
        URI sourceId = sourceDefinition.getClassLoaderId();
        ClassLoader sourceLoader = classLoaderRegistry.getClassLoader(sourceId);
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            List<DataType<?>> sourceTypes = sourceDefinition.getPhysicalDataTypes();
            List<DataType<?>> targetTypes = targetDefinition.getPhysicalDataTypes();
            Interceptor interceptor = transformerFactory.createInterceptor(operation, sourceTypes, targetTypes, targetLoader, sourceLoader);
            // TODO record source and target types on physical operation definition
            chain.addInterceptor(interceptor);
        }
    }


    @SuppressWarnings("unchecked")
    protected <PID extends PhysicalInterceptorDefinition> InterceptorBuilder<PID> getBuilder(PID definition) {
        return (InterceptorBuilder<PID>) interceptorBuilders.get(definition.getClass());

    }

    @SuppressWarnings("unchecked")
    protected <PSD extends PhysicalSourceDefinition> SourceWireAttacher<PSD> getAttacher(PSD source) {
        return (SourceWireAttacher<PSD>) sourceAttachers.get(source.getClass());
    }

    @SuppressWarnings("unchecked")
    protected <PSD extends PhysicalTargetDefinition> TargetWireAttacher<PSD> getAttacher(PSD target) {
        return (TargetWireAttacher<PSD>) targetAttachers.get(target.getClass());
    }
}
