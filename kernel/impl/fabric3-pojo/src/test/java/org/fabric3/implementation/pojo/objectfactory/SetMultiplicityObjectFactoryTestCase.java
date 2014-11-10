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
package org.fabric3.implementation.pojo.objectfactory;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public class SetMultiplicityObjectFactoryTestCase extends TestCase {
    private SetMultiplicityObjectFactory factory = new SetMultiplicityObjectFactory();

    public void testReinjection() throws Exception {
        ObjectFactory<?> mockFactory1 = EasyMock.createMock(ObjectFactory.class);
        ObjectFactory<?> mockFactory2 = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(mockFactory2.getInstance()).andReturn(new Object());
        ObjectFactory<?> mockFactory3 = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(mockFactory3.getInstance()).andReturn(new Object());

        EasyMock.replay(mockFactory1, mockFactory2, mockFactory3);

        factory.startUpdate();
        factory.addObjectFactory(mockFactory1, InjectionAttributes.EMPTY_ATTRIBUTES);
        factory.endUpdate();

        factory.startUpdate();
        factory.addObjectFactory(mockFactory2, InjectionAttributes.EMPTY_ATTRIBUTES);
        factory.endUpdate();
        Set<Object> set = factory.getInstance();
        assertEquals(1, set.size());

        factory.startUpdate();
        factory.addObjectFactory(mockFactory3, InjectionAttributes.EMPTY_ATTRIBUTES);
        factory.endUpdate();
        set = factory.getInstance();
        assertEquals(1, set.size());

        EasyMock.verify(mockFactory1, mockFactory2, mockFactory3);
    }

    public void testSort() throws Exception {
        ObjectFactory<?> mockFactory1 = EasyMock.createMock(ObjectFactory.class);
        Object object1 = new Object();
        EasyMock.expect(mockFactory1.getInstance()).andReturn(object1).times(1);
        InjectionAttributes attributes1 = new InjectionAttributes(null, 2);

        ObjectFactory<?> mockFactory2 = EasyMock.createMock(ObjectFactory.class);
        Object object2 = new Object();
        EasyMock.expect(mockFactory2.getInstance()).andReturn(object2).times(1);
        InjectionAttributes attributes2 = new InjectionAttributes(null, 0);


        EasyMock.replay(mockFactory1, mockFactory2);

        factory.startUpdate();
        factory.addObjectFactory(mockFactory2, attributes2);
        factory.addObjectFactory(mockFactory1, attributes1);
        factory.endUpdate();

        Set<Object> set = factory.getInstance();

        Iterator<Object> iterator = set.iterator();
        assertSame(object2, iterator.next());
        assertSame(object1, iterator.next());

        EasyMock.verify(mockFactory1, mockFactory2);
    }

    public void testNoUpdates() throws Exception {
        ObjectFactory<?> mockFactory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(mockFactory.getInstance()).andReturn(new Object()).times(1);
        EasyMock.replay(mockFactory);

        factory.startUpdate();
        factory.addObjectFactory(mockFactory, InjectionAttributes.EMPTY_ATTRIBUTES);
        factory.endUpdate();

        factory.startUpdate();
        // no update
        factory.endUpdate();

        Set<Object> instance = factory.getInstance();
        assertEquals(1, instance.size());

        EasyMock.verify(mockFactory);
    }

}
