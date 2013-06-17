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
package org.fabric3.implementation.java.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JavaHeuristicTestCase extends TestCase {
    private JavaHeuristic heuristic;
    private IntrospectionHelper helper;
    private InjectingComponentType type;
    private JavaContractProcessor contractProcessor;

    @SuppressWarnings({"unchecked"})
    public void testHeuristicMultipleConstructorsWithAnnotation() throws Exception {

        EasyMock.expect(helper.getInjectionMethods(EasyMock.isA(Class.class),
                                                   EasyMock.isA(Collection.class))).andReturn(Collections.<Method>emptySet());
        EasyMock.expect(helper.getInjectionFields(EasyMock.isA(Class.class))).andReturn(Collections.<Field>emptySet());

        helper.getBaseType(EasyMock.isA(Type.class), EasyMock.isA(TypeMapping.class));

        EasyMock.expectLastCall().andReturn(List.class);
        EasyMock.expect(helper.inferType(EasyMock.isA(Class.class), EasyMock.isA(TypeMapping.class))).andReturn(InjectableType.REFERENCE);

        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class),
                                                     EasyMock.isA(InjectingComponentType.class))).andReturn(new JavaServiceContract());
        EasyMock.replay(helper, contractProcessor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        context.addTypeMapping(MultipleCtorsWithAnnotation.class, new TypeMapping());

        heuristic.applyHeuristics(type, MultipleCtorsWithAnnotation.class, context);
        assertEquals(List.class.getName(), type.getConstructor().getParameterTypes().get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void testHeuristicMultipleConstructors() throws Exception {

        EasyMock.expect(helper.getInjectionMethods(EasyMock.isA(Class.class),
                                                   EasyMock.isA(Collection.class))).andReturn(Collections.<Method>emptySet());
        EasyMock.expect(helper.getInjectionFields(EasyMock.isA(Class.class))).andReturn(Collections.<Field>emptySet());

        helper.getBaseType(EasyMock.isA(Type.class), EasyMock.isA(TypeMapping.class));

        EasyMock.expectLastCall().andReturn(List.class);
        EasyMock.expect(helper.inferType(EasyMock.isA(Class.class), EasyMock.isA(TypeMapping.class))).andReturn(InjectableType.REFERENCE);

        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class),
                                                     EasyMock.isA(InjectingComponentType.class))).andReturn(new JavaServiceContract());
        EasyMock.replay(helper, contractProcessor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        context.addTypeMapping(MultipleCtors.class, new TypeMapping());

        heuristic.applyHeuristics(type, MultipleCtors.class, context);
        assertEquals(List.class.getName(), type.getConstructor().getParameterTypes().get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void setUp() throws Exception {
        super.setUp();
        helper = EasyMock.createNiceMock(IntrospectionHelper.class);
        HeuristicProcessor processor = EasyMock.createMock(HeuristicProcessor.class);
        contractProcessor = EasyMock.createMock(JavaContractProcessor.class);
        heuristic = new JavaHeuristic(helper, contractProcessor, processor);

        type = new InjectingComponentType();
    }


    private static class MultipleCtorsWithAnnotation {
        public MultipleCtorsWithAnnotation() {
        }

        public MultipleCtorsWithAnnotation(@Reference List<Object> services) {
        }

    }

    private static class MultipleCtors {
        public MultipleCtors() {
        }

        @Constructor
        public MultipleCtors(@Reference List<Object> services) {
        }

    }

}
