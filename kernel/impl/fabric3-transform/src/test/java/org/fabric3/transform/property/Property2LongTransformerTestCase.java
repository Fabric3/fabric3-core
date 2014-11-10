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

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Integer transform.
 */
public class Property2LongTransformerTestCase extends BaseTransformTest {

    /**
     * Test of converting String to Long
     */
    public void testLongTransform() {
        final String ANY_LONG = "9965732839230";
        final String xml = "<string_to_long>" + ANY_LONG + "</string_to_long>";
        try {
            Property2LongTransformer transformer = new Property2LongTransformer();
            final long convertedLong = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convertedLong);
            assertEquals(9965732839230l, convertedLong);
        } catch (TransformationException te) {
            fail("Transform exception should not occur " + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test failure of converting String to Integer
     */
    public void testLongTransformFailure() {
        final String NON_INTEGER = "11l";
        final String xml = "<string_to_long>" + NON_INTEGER + "</string_to_long>";
        try {
            Property2LongTransformer transformer = new Property2LongTransformer();
            transformer.transform(getNode(xml), getClass().getClassLoader());
            fail("Should not reach here something wrong in [ String2Long ] code");
        } catch (TransformationException te) {
            assertNotNull(te);
            assertTrue(NumberFormatException.class.isAssignableFrom(te.getCause().getClass()));
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }


}
