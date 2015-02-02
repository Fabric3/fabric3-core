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
package org.fabric3.implementation.pojo.supplier;

import java.util.Map;
import java.util.function.Supplier;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.injection.InjectionAttributes;

/**
 *
 */
public class MapMultiplicitySupplierTestCase extends TestCase {
    private MapMultiplicitySupplier supplier = new MapMultiplicitySupplier();

    public void testReinjection() throws Exception {
        Supplier<?> mockFactory = EasyMock.createMock(Supplier.class);
        EasyMock.expect(mockFactory.get()).andReturn(new Object()).times(2);
        EasyMock.replay(mockFactory);

        supplier.startUpdate();
        InjectionAttributes attributes = new InjectionAttributes("baz", Integer.MIN_VALUE);
        supplier.addSupplier(mockFactory, attributes);
        supplier.endUpdate();

        supplier.startUpdate();
        attributes = new InjectionAttributes("foo", Integer.MIN_VALUE);
        supplier.addSupplier(mockFactory, attributes);
        supplier.endUpdate();
        Map<Object, Object> map = supplier.get();
        assertEquals(1, map.size());

        supplier.startUpdate();
        attributes = new InjectionAttributes("bar", Integer.MIN_VALUE);
        supplier.addSupplier(mockFactory, attributes);
        supplier.endUpdate();
        map = supplier.get();
        assertEquals(1, map.size());

        EasyMock.verify(mockFactory);
    }

    public void testNoUpdates() throws Exception {
        Supplier<?> mockFactory = EasyMock.createMock(Supplier.class);
        EasyMock.expect(mockFactory.get()).andReturn(new Object()).times(1);
        EasyMock.replay(mockFactory);

        supplier.startUpdate();
        InjectionAttributes attributes = new InjectionAttributes("baz", Integer.MIN_VALUE);
        supplier.addSupplier(mockFactory, attributes);
        supplier.endUpdate();

        supplier.startUpdate();
        // no update
        supplier.endUpdate();

        Map<Object, Object> map = supplier.get();
        assertEquals(1, map.size());

        EasyMock.verify(mockFactory);
    }
}
