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
*/
package org.fabric3.implementation.spring.introspection;

import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 *
 */
public class SpringImplementationProcessorImplTestCase extends TestCase {
    private SpringImplementationProcessor processor;

    public void testIntrospectContext() throws Exception {
        URL url = getClass().getResource("simple.context.xml");
        UrlSource source = new UrlSource(url);
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(null, getClass().getClassLoader());
        SpringComponentType type = processor.introspect(source, context);
        assertFalse(context.hasErrors());
        assertEquals(2, type.getBeansByName().size());
        assertEquals(2, type.getSpringServices().size());
        assertEquals(1, type.getReferences().size());
    }

    public void testMissingService() throws Exception {
        URL url = getClass().getResource("invalid.context.xml");
        UrlSource source = new UrlSource(url);
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(null, getClass().getClassLoader());
        processor.introspect(source, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof ServiceTargetNotFound);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JavaContractProcessor contractProcessor = EasyMock.createNiceMock(JavaContractProcessor.class);
        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class), EasyMock.isA(IntrospectionContext.class))).andReturn(null).anyTimes();
        EasyMock.replay(contractProcessor);
        MockXMLFactory xmlFactory = new MockXMLFactory();
        processor = new SpringImplementationProcessorImpl(contractProcessor, xmlFactory);
    }
}