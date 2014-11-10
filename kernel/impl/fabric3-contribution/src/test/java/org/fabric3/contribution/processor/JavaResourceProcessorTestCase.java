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
package org.fabric3.contribution.processor;

import javax.xml.XMLConstants;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ComponentProcessor;

/**
 *
 */
public class JavaResourceProcessorTestCase extends TestCase {
    private JavaResourceProcessor processor;
    private IntrospectionContext context;
    private Resource resource;
    private ComponentProcessor componentProcessor;
    private MetaDataStore metaDataStore;

    public void testIndexAndProcess() throws Exception {
        JavaSymbol symbol = new JavaSymbol(TestComponent.class.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, TestComponent.class);
        resource.addResourceElement(resourceElement);

        componentProcessor.process(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(Class.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall();
        EasyMock.expect(metaDataStore.resolve(EasyMock.eq(URI.create("test")),
                                              EasyMock.eq(Composite.class),
                                              EasyMock.isA(QNameSymbol.class),
                                              EasyMock.eq(context))).andReturn(null);
        EasyMock.replay(componentProcessor, metaDataStore);

        processor.index(resource, context);

        assertFalse(context.hasErrors());

        assertEquals(2, resource.getResourceElements().size());

        processor.process(resource, context);

        assertFalse(context.hasErrors());

        assertEquals(2, resource.getContribution().getResources().size());

        EasyMock.verify(componentProcessor, metaDataStore);
    }

    public void testIllegalCompositeName() throws Exception {
        JavaSymbol symbol = new JavaSymbol(BadTestComponent.class.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, BadTestComponent.class);
        resource.addResourceElement(resourceElement);

        EasyMock.replay(componentProcessor, metaDataStore);

        processor.index(resource, context);

        assertTrue(context.hasErrors());

        EasyMock.verify(componentProcessor, metaDataStore);
    }

    public void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry registry = EasyMock.createNiceMock(ProcessorRegistry.class);

        componentProcessor = EasyMock.createMock(ComponentProcessor.class);

        metaDataStore = EasyMock.createMock(MetaDataStore.class);
        processor = new JavaResourceProcessor(registry, componentProcessor, metaDataStore);

        Contribution contribution = new Contribution(URI.create("test"));
        resource = new Resource(contribution, null, Constants.JAVA_COMPONENT_CONTENT_TYPE);
        contribution.addResource(resource);

        ClassLoader classLoader = getClass().getClassLoader();
        context = new DefaultIntrospectionContext(URI.create("test"), classLoader);
    }

    @Component
    private class TestComponent {

    }

    @Component(composite = "{" + XMLConstants.NULL_NS_URI + "}foo")
    private class BadTestComponent {

    }
}
