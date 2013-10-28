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
package org.fabric3.fabric.introspection;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.processor.ImplementationProcessor;
import static org.fabric3.api.model.type.java.JavaImplementation.IMPLEMENTATION_JAVA;

/**
 *
 */
public class ComponentProcessorImplTestCase extends TestCase {

    private ComponentProcessorImpl processor;
    private DefaultIntrospectionContext context;
    private ImplementationProcessor<?> implementationProcessor;

    @SuppressWarnings("unchecked")
    public void testProcessDefinitionNoImplementation() throws Exception {
        ComponentDefinition definition = new ComponentDefinition("test");

        implementationProcessor.process(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(Class.class), EasyMock.isA(IntrospectionContext.class));

        EasyMock.replay(implementationProcessor);

        processor.process(definition, TestComponent.class, context);

        EasyMock.verify(implementationProcessor);
    }

    @SuppressWarnings("unchecked")
    public void testProcessDefinition() throws Exception {
        ComponentDefinition definition = new ComponentDefinition("test");
        JavaImplementation implementation = new JavaImplementation();
        definition.setImplementation(implementation);

        implementationProcessor.process(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(IntrospectionContext.class));

        EasyMock.replay(implementationProcessor);

        processor.process(definition, context);

        EasyMock.verify(implementationProcessor);
    }

    public void setUp() throws Exception {
        super.setUp();

        implementationProcessor = EasyMock.createMock(ImplementationProcessor.class);

        processor = new ComponentProcessorImpl();
        Map<QName, ImplementationProcessor<?>> map = Collections.<QName, ImplementationProcessor<?>>singletonMap(IMPLEMENTATION_JAVA, implementationProcessor);
        processor.setImplementationProcessors(map);

        ClassLoader classLoader = getClass().getClassLoader();
        context = new DefaultIntrospectionContext(URI.create("test"), classLoader);
    }

    @Component
    private class TestComponent {

    }
}
