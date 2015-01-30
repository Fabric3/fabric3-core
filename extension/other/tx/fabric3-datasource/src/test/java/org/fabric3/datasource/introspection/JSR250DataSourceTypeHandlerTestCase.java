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
 */
package org.fabric3.datasource.introspection;

import javax.sql.DataSource;
import java.lang.reflect.Field;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.Resource;
import org.fabric3.datasource.model.DataSourceResourceReference;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JSR250DataSourceTypeHandlerTestCase extends TestCase {
    private DefaultIntrospectionContext context;
    private JavaContractProcessor processor;
    private JSR250DataSourceTypeHandler handler;

    public void testName() throws Exception {
        Field field = Test.class.getDeclaredField("source");
        Resource resource = field.getAnnotation(Resource.class);
        DataSourceResourceReference definition = handler.createResourceReference("resource", resource, field, null, context);
        assertEquals("resource", definition.getName());
        assertEquals("datasource", definition.getDataSourceName());
        assertNotNull(definition.getServiceContract());
        EasyMock.verify(processor);
    }

    public void testOptional() throws Exception {
        Field field = Test.class.getDeclaredField("optional");
        Resource resource = field.getAnnotation(Resource.class);
        DataSourceResourceReference definition = handler.createResourceReference("resource", resource, field, null, context);
        assertEquals("resource", definition.getName());
        assertEquals("datasource", definition.getDataSourceName());
        assertNotNull(definition.getServiceContract());
        assertTrue(definition.isOptional());

        EasyMock.verify(processor);
    }

    public void testMissingName() throws Exception {
        Field field = Test.class.getDeclaredField("badSource");
        Resource resource = field.getAnnotation(Resource.class);
        handler.createResourceReference("resource", resource, field, null, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingDataSourceName);

        EasyMock.verify(processor);
    }

    protected void setUp() throws Exception {
        super.setUp();
        processor = EasyMock.createMock(JavaContractProcessor.class);
        processor.introspect(EasyMock.eq(DataSource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(new JavaServiceContract(DataSource.class));
        EasyMock.replay(processor);

        handler = new JSR250DataSourceTypeHandler(processor);
        handler.init();

        context = new DefaultIntrospectionContext();
    }


    private class Test {
        @Resource(name = "datasource")
        protected DataSource source;

        @Resource
        protected DataSource badSource;

        @Resource(name = "datasource", optional = true)
        protected DataSource optional;

    }
}