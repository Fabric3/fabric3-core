/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.fabric.container.builder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.container.wire.InvocationChainImpl;
import org.fabric3.fabric.container.wire.WireImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.BuildException;
import org.fabric3.spi.container.builder.Connector;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InterceptorCreationException;
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
    public void setSourceAttachers(Map<Class<? extends PhysicalWireSourceDefinition>,
            SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference
    public void setTargetAttachers(Map<Class<? extends PhysicalWireTargetDefinition>,
            TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    public void connect(PhysicalWireDefinition definition) throws BuildException {
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

    public void disconnect(PhysicalWireDefinition definition) throws BuildException {
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

    Wire createWire(PhysicalWireDefinition definition) throws BuildException {
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
     * @throws BuildException if there is an error creating a transformer
     */
    private void processTransform(Wire wire, PhysicalWireDefinition definition) throws BuildException {
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
     * @throws BuildException if there is an error creating a transformer
     */
    private void addTransformer(Wire wire, PhysicalWireDefinition definition, boolean checkPassByRef) throws BuildException {
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
            try {
                Interceptor interceptor = transformerFactory.createInterceptor(operation, sourceTypes, targetTypes, targetLoader, sourceLoader);
                chain.addInterceptor(interceptor);
            } catch (InterceptorCreationException e) {
                throw new BuildException(e);
            }
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
