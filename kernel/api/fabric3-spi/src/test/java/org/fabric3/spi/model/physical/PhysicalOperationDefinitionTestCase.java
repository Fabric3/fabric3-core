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
package org.fabric3.spi.model.physical;

import junit.framework.TestCase;

/**
 *
 */
public class PhysicalOperationDefinitionTestCase extends TestCase {

    public void testCompareTo() {
        PhysicalOperationDefinition definition1 = createDefinition("foo", String.class.getName());
        PhysicalOperationDefinition definition1a = createDefinition("foo", String.class.getName());

        PhysicalOperationDefinition definition2 = createDefinition("foo", Integer.class.getName());
        PhysicalOperationDefinition definition2a = createDefinition("foo", Integer.class.getName());

        assertEquals(0, definition1.compareTo(definition1a));
        assertEquals(0, definition1a.compareTo(definition1));
        assertEquals(0, definition2.compareTo(definition2a));
        assertEquals(0, definition2a.compareTo(definition2));

        int val1to2 = definition1.compareTo(definition2);
        int val1ato2a = definition1a.compareTo(definition2a);
        assertEquals(val1to2, val1ato2a);

        int val2to1 = definition2.compareTo(definition1);
        int val2ato1a = definition2a.compareTo(definition1a);
        assertNotSame(val1to2, val2to1);
        assertNotSame(val1ato2a, val2ato1a);

        assertNotSame(val1to2, val2to1);
        assertNotSame(val1ato2a, val2ato1a);
    }

    private PhysicalOperationDefinition createDefinition(String name, String type) {
        PhysicalOperationDefinition definition = new PhysicalOperationDefinition();
        definition.setName(name);
        definition.addTargetParameterType(type);
        return definition;
    }

}
