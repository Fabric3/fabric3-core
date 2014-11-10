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

import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public class MapMultiplicityObjectFactoryTestCase extends TestCase {
    private MapMultiplicityObjectFactory factory = new MapMultiplicityObjectFactory();

    public void testReinjection() throws Exception {
        ObjectFactory<?> mockFactory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(mockFactory.getInstance()).andReturn(new Object()).times(2);
        EasyMock.replay(mockFactory);

        factory.startUpdate();
        InjectionAttributes attributes = new InjectionAttributes("baz", Integer.MIN_VALUE);
        factory.addObjectFactory(mockFactory, attributes);
        factory.endUpdate();

        factory.startUpdate();
        attributes = new InjectionAttributes("foo", Integer.MIN_VALUE);
        factory.addObjectFactory(mockFactory, attributes);
        factory.endUpdate();
        Map<Object, Object> map = factory.getInstance();
        assertEquals(1, map.size());

        factory.startUpdate();
        attributes = new InjectionAttributes("bar", Integer.MIN_VALUE);
        factory.addObjectFactory(mockFactory, attributes);
        factory.endUpdate();
        map = factory.getInstance();
        assertEquals(1, map.size());

        EasyMock.verify(mockFactory);
    }

    public void testNoUpdates() throws Exception {
        ObjectFactory<?> mockFactory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(mockFactory.getInstance()).andReturn(new Object()).times(1);
        EasyMock.replay(mockFactory);

        factory.startUpdate();
        InjectionAttributes attributes = new InjectionAttributes("baz", Integer.MIN_VALUE);
        factory.addObjectFactory(mockFactory, attributes);
        factory.endUpdate();

        factory.startUpdate();
        // no update
        factory.endUpdate();

        Map<Object, Object> map = factory.getInstance();
        assertEquals(1, map.size());

        EasyMock.verify(mockFactory);
    }
}
