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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transform.property;


/**
 * Tests String to boolean transform.
 */
public class Property2BooleanTestCase extends BaseTransformTest {

    /**
     * Test of converting String to Boolean on true
     */
    public void testBooleanTransformForTrue() {
        final String TRUE = "true";
        final String xml = "<string_to_boolean>" + TRUE + "</string_to_boolean>";
        try {
            Property2BooleanTransformer transformer = new Property2BooleanTransformer();
            final boolean convBoolean = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convBoolean);
            assertTrue(convBoolean);
        } catch (Exception e) {
            fail("Unexpected Exception Should not occur " + e);
        }
    }

    /**
     * Test failure of converting String to boolean on False
     */
    public void testBooleanTransformForFalse() {
        final String FALSE = "false";
        final String xml = "<string_to_boolean>" + FALSE + "</string_to_boolean>";
        try {
            Property2BooleanTransformer transformer = new Property2BooleanTransformer();
            boolean convBoolean = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convBoolean);
            assertFalse(convBoolean);
        } catch (Exception e) {
            fail("Unexpected Exception Should not occur " + e);
        }
    }

    /**
     * Test failure of converting String to boolean on False
     */
    public void testBooleanOnUnspecifiedFalse() {
        final String FALSE = "SHOULD BE FALSE";
        final String xml = "<string_to_boolean>" + FALSE + "</string_to_boolean>";
        try {
            Property2BooleanTransformer transformer = new Property2BooleanTransformer();
            boolean convBoolean = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convBoolean);
            assertFalse(convBoolean);
        } catch (Exception e) {
            fail("Unexpected Exception Should not occur " + e);
        }
    }

}
