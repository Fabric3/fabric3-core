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
 * Tests String to Integer transform.
 */
public class Property2ShortTransformerTestCase extends BaseTransformTest {

    /**
     * Test of converting String to Short
     */
    public void testShortTransform() {
        final String ANY_SHORT = "153";
        final String xml = "<string_to_short>" + ANY_SHORT + "</string_to_short>";
        try {
            Property2ShortTransformer transformer = new Property2ShortTransformer();
            final short convertedShort = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convertedShort);
            assertEquals(153, convertedShort);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }


}
