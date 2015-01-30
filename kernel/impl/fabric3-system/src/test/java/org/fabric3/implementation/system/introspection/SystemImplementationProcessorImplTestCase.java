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
package org.fabric3.implementation.system.introspection;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;

/**
 *
 */
public class SystemImplementationProcessorImplTestCase extends TestCase {
    private SystemImplementationIntrospectorImpl loader;
    private ClassVisitor classVisitor;
    private IntrospectionContext context;
    private HeuristicProcessor heuristic;
    private IMocksControl control;

    public void testSimple() throws IntrospectionException {

        classVisitor.visit(EasyMock.isA(InjectingComponentType.class), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        heuristic.applyHeuristics(EasyMock.isA(InjectingComponentType.class), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        control.replay();
        InjectingComponentType componentType = new InjectingComponentType(Simple.class.getName());
        loader.introspect(componentType, context);

        assertNotNull(componentType);
        assertEquals(Simple.class.getName(), componentType.getImplClass());
        control.verify();
    }

    private static class Simple {
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();

        IntrospectionHelper helper = EasyMock.createMock(IntrospectionHelper.class);
        helper.loadClass(EasyMock.isA(String.class), EasyMock.isA(ClassLoader.class));
        EasyMock.expectLastCall().andReturn(Simple.class);
        helper.resolveTypeParameters(EasyMock.isA(Class.class), EasyMock.isA(TypeMapping.class));
        EasyMock.replay(helper);


        context = new DefaultIntrospectionContext(URI.create("test"), getClass().getClassLoader());
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(Simple.class, mapping);

        control = EasyMock.createControl();
        classVisitor = control.createMock(ClassVisitor.class);
        heuristic = control.createMock(HeuristicProcessor.class);

        this.loader = new SystemImplementationIntrospectorImpl(classVisitor, heuristic, helper);
    }
}
