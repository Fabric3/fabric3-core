/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import org.fabric3.fabric.builder.transform.TransformerInterceptorFactory;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorImplTestCase extends TestCase {
    private static final DataType<?> DATA_TYPE = new XSDSimpleType(Document.class, new QName(XSDType.XSD_NS, "string"));
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

        Map sourceAttachers = Collections.singletonMap(MockSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTargetDefinition.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class), EasyMock.isA(Wire.class));

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
        Map sourceAttachers = Collections.singletonMap(MockSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTargetDefinition.class, targetAttacher);
        sourceAttacher.detach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class));
        targetAttacher.detach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class));
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
        Map sourceAttachers = Collections.singletonMap(MockSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTargetDefinition.class, targetAttacher);
        sourceAttacher.attachObjectFactory(EasyMock.isA(PhysicalSourceDefinition.class),
                                           EasyMock.isA(ObjectFactory.class),
                                           EasyMock.isA(PhysicalTargetDefinition.class));
        targetAttacher.createObjectFactory(EasyMock.isA(PhysicalTargetDefinition.class));
        EasyMock.expectLastCall().andReturn(new SingletonObjectFactory<Object>(new Object()));
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
        Map sourceAttachers = Collections.singletonMap(MockSourceDefinition.class, sourceAttacher);
        Map targetAttachers = Collections.singletonMap(MockTargetDefinition.class, targetAttacher);
        sourceAttacher.attach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class), EasyMock.isA(Wire.class));
        targetAttacher.attach(EasyMock.isA(PhysicalSourceDefinition.class), EasyMock.isA(PhysicalTargetDefinition.class), EasyMock.isA(Wire.class));

        definition.getTarget().getPhysicalDataTypes().clear();
        definition.getTarget().getPhysicalDataTypes().add(DATA_TYPE);
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
        builders = new HashMap<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?>>();
        connector.setInterceptorBuilders(builders);

        createDefinition();
    }

    private void createDefinition() {
        PhysicalSourceDefinition sourceDefinition = new MockSourceDefinition();
        sourceDefinition.setUri(URI.create("source"));
        PhysicalTargetDefinition targetDefinition = new MockTargetDefinition();
        targetDefinition.setUri(URI.create("target"));
        Set<PhysicalOperationDefinition> operations = new HashSet<PhysicalOperationDefinition>();
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

    private class MockSourceDefinition extends PhysicalSourceDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

    private class MockTargetDefinition extends PhysicalTargetDefinition {
        private static final long serialVersionUID = 3221998280377320208L;

    }

}
