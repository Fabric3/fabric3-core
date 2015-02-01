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

import org.fabric3.api.host.Fabric3Exception;

/**
 * Tests String to Integer Transform
 */
public class Property2IntegerTransformerTestCase extends BaseTransformTest {

    /**
     * Test of converting String to Integer
     */
    public void testIntegerTransform() {
        final String ANY_NUMBER = "99";
        final String xml = "<string_to_integer>" + ANY_NUMBER + "</string_to_integer>";
        try {
            Property2IntegerTransformer transformer = new Property2IntegerTransformer();
            final int convertedInt = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(convertedInt);
            assertEquals(99, convertedInt);
        } catch (Fabric3Exception te) {
            fail("Transform exception should not occur " + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test failure of converting String to Integer
     */
    public void testIntegerTransformFailure() {
        final String NON_INTEGER = "1009876548888899";
        final String xml = "<string_to_integer>" + NON_INTEGER + "</string_to_integer>";
        try {
            Property2IntegerTransformer transformer = new Property2IntegerTransformer();
            transformer.transform(getNode(xml), getClass().getClassLoader());
            fail("Should not reach here something wrong in [ String2Integer ] code");
        } catch (Fabric3Exception te) {
            assertNotNull(te);
            assertTrue(NumberFormatException.class.isAssignableFrom(te.getCause().getClass()));
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

}
