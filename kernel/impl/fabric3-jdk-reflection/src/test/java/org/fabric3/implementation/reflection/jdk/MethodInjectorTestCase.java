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
package org.fabric3.implementation.reflection.jdk;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;

/**
 *
 */
public class MethodInjectorTestCase extends TestCase {
    private Method fooMethod;
    private Method exceptionMethod;
    private Supplier supplier;

    public void testIllegalArgument() throws Exception {
        EasyMock.expect(supplier.get()).andReturn(new Object());
        EasyMock.replay(supplier);
        MethodInjector injector = new MethodInjector(fooMethod, supplier);
        try {
            injector.inject(new Foo());
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
    }

    public void testException() throws Exception {
        EasyMock.expect(supplier.get()).andReturn("foo");
        EasyMock.replay(supplier);
        MethodInjector injector = new MethodInjector(exceptionMethod, supplier);
        try {
            injector.inject(new Foo());
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
    }

    public void testReinjectionOfNullValue() throws Exception {
        EasyMock.replay(supplier);
        MethodInjector injector = new MethodInjector(fooMethod, supplier);
        try {
            injector.clearSupplier();
            Foo foo = new Foo();
            injector.inject(foo);
            assertNull(foo.getFoo());
        } catch (Fabric3Exception e) {
            // expected
        }
    }


    protected void setUp() throws Exception {
        super.setUp();
        fooMethod = Foo.class.getMethod("setFoo", String.class);
        exceptionMethod = Foo.class.getDeclaredMethod("exception", String.class);
        supplier = EasyMock.createMock(Supplier.class);
    }

    private class Foo {
        private String foo = "default";

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        private void hidden(String bar) {
        }

        public void exception(String bar) {
            throw new RuntimeException();
        }

    }
}
