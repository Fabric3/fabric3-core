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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.api.annotation.Consumer;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.model.type.java.InjectingComponentType;

public class ConsumerProcessorTestCase extends TestCase {
    private ConsumerProcessor processor;
    private InjectingComponentType componentType;
    private DefaultIntrospectionContext context;

    public void testMethod() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("onEvent", String.class);
        Consumer annotation = method.getAnnotation(Consumer.class);
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());

        Map<String, ConsumerDefinition> consumers = componentType.getConsumers();
        ConsumerDefinition definition = consumers.get("onEvent");
        assertEquals(1, definition.getTypes().size());
        assertEquals(String.class, definition.getTypes().get(0).getPhysical());
    }

    public void testSequenceMethod() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("onSequenceEvent", String.class);
        Consumer annotation = method.getAnnotation(Consumer.class);
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(TestClass.class, mapping);

        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getErrors().size());

        Map<String, ConsumerDefinition> consumers = componentType.getConsumers();
        ConsumerDefinition definition = consumers.get("onSequenceEvent");

        assertEquals(2, definition.getSequence());
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new ConsumerProcessor(helper);
        componentType = new InjectingComponentType();

        context = new DefaultIntrospectionContext();
    }

    public static class TestClass {

        @Consumer
        public void onEvent(String message) {

        }

        @Consumer(sequence = 2)
        public void onSequenceEvent(String message) {

        }

    }

}