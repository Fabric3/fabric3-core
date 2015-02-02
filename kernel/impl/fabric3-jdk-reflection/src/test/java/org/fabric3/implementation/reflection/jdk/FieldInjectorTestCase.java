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

import java.lang.reflect.Field;
import java.util.function.Supplier;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class FieldInjectorTestCase extends TestCase {

    protected Field protectedField;
    private Field fooField;
    private Supplier<String> supplier;
    private Foo foo;

    public void testIllegalAccess() throws Exception {
        String value = "foo";
        EasyMock.expect(supplier.get()).andReturn(value);
        EasyMock.replay(supplier);

        FieldInjector injector = new FieldInjector(protectedField, supplier);
        injector.inject(foo);
        assertEquals(value, foo.hidden);
    }

    public void testReinjectionOfNullValue() throws Exception {
        EasyMock.replay(supplier);

        FieldInjector injector = new FieldInjector(fooField, supplier);
        injector.clearSupplier();
        injector.inject(foo);
        assertNull(foo.foo);
    }


    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        protectedField = Foo.class.getDeclaredField("hidden");
        fooField = Foo.class.getDeclaredField("foo");
        supplier = EasyMock.createMock(Supplier.class);
        foo = new Foo();
    }

    private class Foo {
        private String hidden;
        public String foo = "default";


    }
}
