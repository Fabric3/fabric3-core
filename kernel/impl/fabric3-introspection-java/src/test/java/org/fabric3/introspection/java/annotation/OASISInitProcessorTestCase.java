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
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Init;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.InjectingComponentType;

@SuppressWarnings("unchecked")
public class OASISInitProcessorTestCase extends TestCase {
    private OASISInitProcessor processor;
    private InjectingComponentType componentType;

    public void testInvalidStringReturnInit() throws Exception {
        TestInvalidInitClass componentToProcess = new TestInvalidInitClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        processor.visitMethod(annotation, TestInvalidInitClass.class.getDeclaredMethod("init"), TestClass.class, componentType, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidMethod);
    }

    public void testInit() throws Exception {
        TestClass componentToProcess = new TestClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        Method method = TestClass.class.getDeclaredMethod("init");
        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getWarnings().size());
        assertEquals(method, componentType.getInitMethod().getMethod(TestClass.class));
    }

    public void testPrivateInit() throws Exception {
        TestPrivateInitClass componentToProcess = new TestPrivateInitClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        Method method = TestClass.class.getDeclaredMethod("init");
        processor.visitMethod(annotation, method, TestClass.class, componentType, context);
        assertEquals(0, context.getWarnings().size());
        assertEquals(method, componentType.getInitMethod().getMethod(TestClass.class));
    }

    protected void setUp() throws Exception {
        super.setUp();
        processor = new OASISInitProcessor();
        componentType = new InjectingComponentType();
    }

    public static class TestClass {
        @Init
        public void init() {

        }
    }

    public static class TestPrivateInitClass {
        @Init
        private void init() {

        }
    }

    public static class TestInvalidInitClass {
        @Init
        public String init() {
            return "test";
        }
    }

}