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
*/
package org.fabric3.datasource.introspection;

import java.lang.reflect.Field;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.annotation.Resource;
import org.fabric3.datasource.model.DataSourceResourceReference;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceTypeHandlerTestCase extends TestCase {
    private DefaultIntrospectionContext context;
    private JavaContractProcessor processor;
    private DataSourceTypeHandler handler;

    public void testName() throws Exception {
        Field field = Test.class.getDeclaredField("source");
        Resource resource = field.getAnnotation(Resource.class);
        DataSourceResourceReference definition = handler.createResourceReference("resource", resource, field, context);
        assertEquals("resource", definition.getName());
        assertEquals("datasource", definition.getDataSourceName());
        assertNotNull(definition.getServiceContract());
        EasyMock.verify(processor);
    }

    public void testOptional() throws Exception {
        Field field = Test.class.getDeclaredField("optional");
        Resource resource = field.getAnnotation(Resource.class);
        DataSourceResourceReference definition = handler.createResourceReference("resource", resource, field, context);
        assertEquals("resource", definition.getName());
        assertEquals("datasource", definition.getDataSourceName());
        assertNotNull(definition.getServiceContract());
        assertTrue(definition.isOptional());

        EasyMock.verify(processor);
    }

    public void testMissingName() throws Exception {
        Field field = Test.class.getDeclaredField("badSource");
        Resource resource = field.getAnnotation(Resource.class);
        handler.createResourceReference("resource", resource, field, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingDataSourceName);

        EasyMock.verify(processor);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        processor = EasyMock.createMock(JavaContractProcessor.class);
        processor.introspect(EasyMock.eq(DataSource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(new JavaServiceContract(DataSource.class));
        EasyMock.replay(processor);

        handler = new DataSourceTypeHandler(processor);
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