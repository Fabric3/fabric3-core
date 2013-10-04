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
