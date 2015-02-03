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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 *
 */
public class ConnectorImplTestCase extends TestCase {
    private ConnectorImpl connector;
    private PhysicalWireDefinition definition;
    private PhysicalOperationDefinition operation;
    private PhysicalOperationDefinition callback;

    @SuppressWarnings({"unchecked"})
    public void testConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);

        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));

        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;

        connector.connect(definition);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    public void testDisconnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.detach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class));
        targetAttacher.detach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class));
        EasyMock.replay(sourceAttacher, targetAttacher);

        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;

        connector.disconnect(definition);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    public void testOptimizedConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.attachSupplier(EasyMock.isA(PhysicalWireSourceDefinition.class),
                                      EasyMock.isA(Supplier.class),
                                      EasyMock.isA(PhysicalWireTargetDefinition.class));
        targetAttacher.createSupplier(EasyMock.isA(PhysicalWireTargetDefinition.class));
        EasyMock.expectLastCall().andReturn((Supplier) Object::new);
        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;
        definition.setOptimizable(true);

        connector.connect(definition);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    public void testCreateWire() throws Exception {
        Wire wire = connector.createWire(definition);
        assertEquals(2, wire.getInvocationChains().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDispatchToBuilder() throws Exception {
        InterceptorBuilder builder = EasyMock.createMock(InterceptorBuilder.class);
        EasyMock.expect(builder.build(EasyMock.isA(PhysicalInterceptorDefinition.class))).andReturn(null).times(2);
        EasyMock.replay(builder);
        Map<Class<?>, InterceptorBuilder<?>> builders = new HashMap<>();

        builders.put(PhysicalInterceptorDefinition.class, builder);

        connector.interceptorBuilders = builders;
        PhysicalInterceptorDefinition interceptorDefinition = new PhysicalInterceptorDefinition();
        operation.addInterceptor(interceptorDefinition);
        callback.addInterceptor(interceptorDefinition);

        connector.createWire(definition);
        EasyMock.verify(builder);
    }

    protected void setUp() throws Exception {
        super.setUp();
        connector = new ConnectorImpl();
        createDefinition();
    }

    private void createDefinition() {
        PhysicalWireSourceDefinition sourceDefinition = new MockWireSourceDefinition();
        sourceDefinition.setUri(URI.create("source"));
        PhysicalWireTargetDefinition targetDefinition = new MockWireTargetDefinition();
        targetDefinition.setUri(URI.create("target"));
        Set<PhysicalOperationDefinition> operations = new HashSet<>();
        definition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        URI classLoaderUri = URI.create("classloader");
        sourceDefinition.setClassLoaderId(classLoaderUri);
        targetDefinition.setClassLoaderId(classLoaderUri);

        operation = new PhysicalOperationDefinition();
        operation.setName("operation");
        definition.addOperation(operation);
        callback = new PhysicalOperationDefinition();
        callback.setName("callback");
        callback.setCallback(true);
        definition.addOperation(callback);
    }

    private class MockWireSourceDefinition extends PhysicalWireSourceDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

    private class MockWireTargetDefinition extends PhysicalWireTargetDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

}
