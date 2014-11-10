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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;

import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.Wire;

/**
 *
 */
public class ConnectorImplTestCase extends TestCase {
    private static final DataType DATA_TYPE = new XSDSimpleType(Document.class, new QName(XSDType.XSD_NS, "string"));
    private ConnectorImpl connector;
    private PhysicalWireDefinition definition;
    private PhysicalOperationDefinition operation;
    private PhysicalOperationDefinition callback;
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>> builders;
    private TransformerInterceptorFactory transformerFactory;

    @SuppressWarnings({"unchecked"})
    public void testConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);

        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));

        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

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

        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

        connector.disconnect(definition);
        EasyMock.verify(sourceAttacher, targetAttacher);
    }

    @SuppressWarnings({"unchecked"})
    public void testOptimizedConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.attachObjectFactory(EasyMock.isA(PhysicalWireSourceDefinition.class),
                                           EasyMock.isA(ObjectFactory.class),
                                           EasyMock.isA(PhysicalWireTargetDefinition.class));
        targetAttacher.createObjectFactory(EasyMock.isA(PhysicalWireTargetDefinition.class));
        EasyMock.expectLastCall().andReturn(new SingletonObjectFactory<>(new Object()));
        EasyMock.replay(sourceAttacher, targetAttacher);
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);
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
        builders.put(PhysicalInterceptorDefinition.class, builder);

        PhysicalInterceptorDefinition interceptorDefinition = new PhysicalInterceptorDefinition();
        operation.addInterceptor(interceptorDefinition);
        callback.addInterceptor(interceptorDefinition);

        connector.createWire(definition);
        EasyMock.verify(builder);
    }

    @SuppressWarnings({"unchecked"})
    public void testTransformerConnect() throws Exception {
        SourceWireAttacher sourceAttacher = EasyMock.createMock(SourceWireAttacher.class);
        TargetWireAttacher targetAttacher = EasyMock.createMock(TargetWireAttacher.class);
        Map sourceAttachers = Collections.singletonMap(MockWireSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockWireTargetDefinition.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalWireSourceDefinition.class), EasyMock.isA(PhysicalWireTargetDefinition.class), EasyMock.isA(Wire.class));

        definition.getTarget().getDataTypes().clear();
        definition.getTarget().getDataTypes().add(DATA_TYPE);
        Interceptor interceptor = EasyMock.createMock(Interceptor.class);

        transformerFactory.createInterceptor(EasyMock.isA(PhysicalOperationDefinition.class),
                                             EasyMock.isA(List.class),
                                             EasyMock.isA(List.class),
                                             EasyMock.isA(ClassLoader.class),
                                             EasyMock.isA(ClassLoader.class));
        EasyMock.expectLastCall().andReturn(interceptor).times(2);
        EasyMock.replay(sourceAttacher, targetAttacher, transformerFactory, interceptor);
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);
        connector.setTransform(true);

        connector.connect(definition);
        EasyMock.verify(sourceAttacher, targetAttacher, transformerFactory, interceptor);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClassLoaderRegistry classLoaderRegistry = EasyMock.createMock((ClassLoaderRegistry.class));
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader()).anyTimes();
        EasyMock.replay(classLoaderRegistry);

        transformerFactory = EasyMock.createMock(TransformerInterceptorFactory.class);

        connector = new ConnectorImpl(classLoaderRegistry, transformerFactory);
        builders = new HashMap<>();
        connector.setInterceptorBuilders(builders);

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
