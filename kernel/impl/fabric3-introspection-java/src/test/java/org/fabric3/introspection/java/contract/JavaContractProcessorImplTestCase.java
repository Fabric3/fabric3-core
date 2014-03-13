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
package org.fabric3.introspection.java.contract;

import javax.jws.WebMethod;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.oasisopen.sca.annotation.Callback;

/**
 *
 */
public class JavaContractProcessorImplTestCase extends TestCase {
    private JavaContractProcessor impl;

    public void testSimpleInterface() {
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(Simple.class, mapping);
        ServiceContract contract = impl.introspect(Simple.class, context);
        assertEquals("Simple", contract.getInterfaceName());
        assertEquals(Simple.class.getName(), contract.getQualifiedInterfaceName());
        List<Operation> operations = contract.getOperations();
        assertEquals(1, operations.size());
        Operation baseInt = operations.get(0);
        assertNotNull(baseInt);

        DataType returnType = baseInt.getOutputType();
        assertEquals(Integer.TYPE, returnType.getPhysical());

        List<?> parameterTypes = baseInt.getInputTypes();
        assertEquals(1, parameterTypes.size());
        DataType arg0 = (DataType) parameterTypes.get(0);
        assertEquals(Integer.TYPE, arg0.getPhysical());

        List<?> faultTypes = baseInt.getFaultTypes();
        assertEquals(1, faultTypes.size());
        DataType fault0 = (DataType) faultTypes.get(0);
        assertEquals(IllegalArgumentException.class, fault0.getPhysical());
    }

    public void testBoundGenericInterface() {
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        helper.resolveTypeParameters(Generic.class, mapping);
        context.addTypeMapping(Generic.class, mapping);
        ServiceContract contract = impl.introspect(Generic.class, context);
        assertEquals("Generic", contract.getInterfaceName());

        List<Operation> operations = contract.getOperations();
        assertEquals(2, operations.size());
        Operation operation = null;
        for (Operation op : operations) {
            if ("echo".equals(op.getName())) {
                operation = op;
                break;
            }
        }
        assertNotNull(operation);

        JavaGenericType returnType = (JavaGenericType) operation.getOutputType();
        // the return type should be unbound, which means the raw type (Base) will be used for the actual parameter type
        JavaTypeInfo info = returnType.getTypeInfo();
        assertEquals(Base.class, info.getRawType());
        assertTrue(info.getParameterTypesInfos().isEmpty());
        assertEquals(Base.class, returnType.getPhysical());

    }

    public void testMethodGeneric() {
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(Generic.class, mapping);
        ServiceContract contract = impl.introspect(Generic.class, context);
        List<Operation> operations = contract.getOperations();
        Operation operation = null;
        for (Operation op : operations) {
            if ("echo2".equals(op.getName())) {
                operation = op;
                break;
            }
        }
        assertNotNull(operation);

        assertEquals("echo2", operation.getName());
    }

    public void testCallbackInterface() {
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(ForwardInterface.class, mapping);
        mapping = new TypeMapping();
        context.addTypeMapping(CallbackInterface.class, mapping);

        ServiceContract contract = impl.introspect(ForwardInterface.class, context);
        ServiceContract callback = contract.getCallbackContract();
        assertEquals("CallbackInterface", callback.getInterfaceName());
        assertEquals(CallbackInterface.class.getName(), callback.getQualifiedInterfaceName());
        List<? extends Operation> operations = callback.getOperations();
        assertEquals(1, operations.size());
        Operation back = operations.get(0);
        assertEquals("back", back.getName());
    }

    public void testMappedWsdlName() throws Exception {
        IntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping mapping = new TypeMapping();
        context.addTypeMapping(FooWsdl.class, mapping);

        ServiceContract contract = impl.introspect(FooWsdl.class, context);

        assertEquals(1, contract.getOperations().size());
        assertEquals("operation", contract.getOperations().get(0).getWsdlName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        impl = new JavaContractProcessorImpl(helper);
    }

    private static interface Base {
        int baseInt(int param) throws IllegalArgumentException;
    }

    private static interface Simple extends Base {
    }

    private static interface Generic<T extends Base> {
        T echo(T t);

        <Q extends Collection<?>> Q echo2(Q q);
    }

    private static class GenericImpl<T extends Base> implements Generic<T> {
        public T echo(T t) {
            return t;
        }

        public <Q extends Collection<?>> Q echo2(Q q) {
            return q;
        }
    }

    private static class BoundImpl extends GenericImpl<Simple> {
    }

    @Callback(CallbackInterface.class)
    private static interface ForwardInterface {
        int forward() throws IllegalArgumentException;
    }

    private static interface CallbackInterface {
        int back() throws IllegalArgumentException;
    }

    private interface FooWsdl {

        @WebMethod(operationName = "operation")
        void op();
    }

}
