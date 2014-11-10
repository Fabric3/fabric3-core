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