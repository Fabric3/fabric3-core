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
package org.fabric3.contribution.processor;

import javax.xml.XMLConstants;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
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

    public void testIndexAndProcess() throws Exception {
        JavaSymbol symbol = new JavaSymbol(TestComponent.class.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, TestComponent.class);
        resource.addResourceElement(resourceElement);

        componentProcessor.process(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(Class.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall();
        EasyMock.replay(componentProcessor);

        processor.index(resource, context);

        assertFalse(context.hasErrors());

        assertEquals(2, resource.getResourceElements().size());

        EasyMock.verify(componentProcessor);

        processor.process(resource, context);

        assertFalse(context.hasErrors());

        assertEquals(2, resource.getContribution().getResources().size());
    }

    public void testIllegalCompositeName() throws Exception {
        JavaSymbol symbol = new JavaSymbol(BadTestComponent.class.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, BadTestComponent.class);
        resource.addResourceElement(resourceElement);

        EasyMock.replay(componentProcessor);

        processor.index(resource, context);

        assertTrue(context.hasErrors());

        EasyMock.verify(componentProcessor);
    }

    public void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry registry = EasyMock.createNiceMock(ProcessorRegistry.class);

        componentProcessor = EasyMock.createMock(ComponentProcessor.class);

        processor = new JavaResourceProcessor(registry, componentProcessor);

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
