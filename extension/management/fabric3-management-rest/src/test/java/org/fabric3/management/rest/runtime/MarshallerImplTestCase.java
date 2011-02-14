/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev: 9966 $ $Date: 2011-02-09 18:47:30 +0100 (Wed, 09 Feb 2011) $
 */
public class MarshallerImplTestCase extends TestCase {
    private Method testString;
    private Method testShort;
    private Method testInteger;
    private Method testLong;
    private Method testDouble;
    private Method testFloat;

    public void testDeserialize() throws Exception {
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        MarshallerImpl deserializer = new MarshallerImpl(monitor);
        assertEquals("test", deserializer.deserialize("test", testString));
        assertEquals((short) 1, deserializer.deserialize("1", testShort));
        assertEquals(1, deserializer.deserialize("1", testInteger));
        assertEquals(Long.MAX_VALUE, deserializer.deserialize(Long.toString(Long.MAX_VALUE), testLong));
        assertEquals(Double.MAX_VALUE, deserializer.deserialize(Double.toString(Double.MAX_VALUE), testDouble));
        assertEquals(1.1f, deserializer.deserialize("1.1", testFloat));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testString = getClass().getDeclaredMethod("testString", String.class);
        testShort = getClass().getDeclaredMethod("testShort", Short.class);
        testInteger = getClass().getDeclaredMethod("testInteger", Integer.class);
        testLong = getClass().getDeclaredMethod("testLong", Long.class);
        testDouble = getClass().getDeclaredMethod("testDouble", Double.class);
        testFloat = getClass().getDeclaredMethod("testFloat", Float.class);

    }

    private void testString(String param) {

    }

    private void testShort(Short param) {

    }

    private void testInteger(Integer param) {

    }

    private void testLong(Long param) {

    }

    private void testDouble(Double param) {

    }

    private void testFloat(Float param) {

    }


}
