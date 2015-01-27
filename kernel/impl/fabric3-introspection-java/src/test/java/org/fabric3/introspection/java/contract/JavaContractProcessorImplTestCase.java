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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
        assertEquals(Integer.TYPE, returnType.getType());

        List<?> parameterTypes = baseInt.getInputTypes();
        assertEquals(1, parameterTypes.size());
        DataType arg0 = (DataType) parameterTypes.get(0);
        assertEquals(Integer.TYPE, arg0.getType());

        List<?> faultTypes = baseInt.getFaultTypes();
        assertEquals(1, faultTypes.size());
        DataType fault0 = (DataType) faultTypes.get(0);
        assertEquals(IllegalArgumentException.class, fault0.getType());
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
        assertEquals(Base.class, returnType.getType());

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
