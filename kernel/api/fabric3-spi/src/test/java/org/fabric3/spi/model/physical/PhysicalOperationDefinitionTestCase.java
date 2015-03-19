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
package org.fabric3.spi.model.physical;

import junit.framework.TestCase;

/**
 *
 */
public class PhysicalOperationDefinitionTestCase extends TestCase {

    public void testCompareTo() {
        PhysicalOperationDefinition definition1 = createDefinition("foo", String.class);
        PhysicalOperationDefinition definition1a = createDefinition("foo", String.class);

        PhysicalOperationDefinition definition2 = createDefinition("foo", Integer.class);
        PhysicalOperationDefinition definition2a = createDefinition("foo", Integer.class);

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

    private PhysicalOperationDefinition createDefinition(String name, Class<?> type) {
        PhysicalOperationDefinition definition = new PhysicalOperationDefinition();
        definition.setName(name);
        definition.addTargetParameterType(type);
        return definition;
    }

}
