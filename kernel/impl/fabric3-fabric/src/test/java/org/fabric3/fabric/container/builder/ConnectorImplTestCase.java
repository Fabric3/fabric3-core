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
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.builder.InterceptorBuilder;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalInterceptor;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWire;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;

/**
 *
 */
public class ConnectorImplTestCase extends TestCase {
    private ConnectorImpl connector;
    private PhysicalWire physicalWire;
    private PhysicalOperation operation;
    private PhysicalOperation callback;

    @SuppressWarnings({"unchecked"})
    public void testConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);

        Map sourceAttachers = Collections.singletonMap(MockWireSource.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTarget.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalWireSource.class), EasyMock.isA(PhysicalWireTarget.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalWireSource.class), EasyMock.isA(PhysicalWireTarget.class), EasyMock.isA(Wire.class));

        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;

        connector.connect(physicalWire);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    public void testDisconnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSource.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTarget.class, targetAttacher);
        sourceAttacher.detach(EasyMock.isA(PhysicalWireSource.class), EasyMock.isA(PhysicalWireTarget.class));
        targetAttacher.detach(EasyMock.isA(PhysicalWireSource.class), EasyMock.isA(PhysicalWireTarget.class));
        EasyMock.replay(sourceAttacher, targetAttacher);

        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;

        connector.disconnect(physicalWire);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    public void testOptimizedConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSource.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTarget.class, targetAttacher);
        sourceAttacher.attachSupplier(EasyMock.isA(PhysicalWireSource.class),
                                      EasyMock.isA(Supplier.class),
                                      EasyMock.isA(PhysicalWireTarget.class));
        targetAttacher.createSupplier(EasyMock.isA(PhysicalWireTarget.class));
        EasyMock.expectLastCall().andReturn((Supplier) Object::new);
        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.sourceAttachers = sourceAttachers;
        connector.targetAttachers = targetAttachers;
        physicalWire.setOptimizable(true);

        connector.connect(physicalWire);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    public void testCreateWire() throws Exception {
        Wire wire = connector.createWire(physicalWire);
        assertEquals(2, wire.getInvocationChains().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDispatchToBuilder() throws Exception {
        InterceptorBuilder builder = EasyMock.createMock(InterceptorBuilder.class);
        EasyMock.expect(builder.build(EasyMock.isA(PhysicalInterceptor.class))).andReturn(null).times(2);
        EasyMock.replay(builder);
        Map<Class<?>, InterceptorBuilder<?>> builders = new HashMap<>();

        builders.put(PhysicalInterceptor.class, builder);

        connector.interceptorBuilders = builders;
        PhysicalInterceptor physicalInterceptor = new PhysicalInterceptor();
        operation.addInterceptor(physicalInterceptor);
        callback.addInterceptor(physicalInterceptor);

        connector.createWire(physicalWire);
        EasyMock.verify(builder);
    }

    protected void setUp() throws Exception {
        super.setUp();
        connector = new ConnectorImpl();
        createDefinition();
    }

    private void createDefinition() {
        PhysicalWireSource sourceDefinition = new MockWireSource();
        sourceDefinition.setUri(URI.create("source"));
        PhysicalWireTarget targetDefinition = new MockWireTarget();
        targetDefinition.setUri(URI.create("target"));
        Set<PhysicalOperation> operations = new HashSet<>();
        physicalWire = new PhysicalWire(sourceDefinition, targetDefinition, operations);
        sourceDefinition.setClassLoader(getClass().getClassLoader());
        targetDefinition.setClassLoader(getClass().getClassLoader());

        operation = new PhysicalOperation();
        operation.setName("operation");
        physicalWire.addOperation(operation);
        callback = new PhysicalOperation();
        callback.setName("callback");
        callback.setCallback(true);
        physicalWire.addOperation(callback);
    }

    private class MockWireSource extends PhysicalWireSource {

    }

    private class MockWireTarget extends PhysicalWireTarget {

    }

}
