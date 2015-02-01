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

import java.text.ParseException;
import java.util.Date;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Tests String to Date transform.
 */
public class Property2DateTestCase extends BaseTransformTest {

    /**
     * Test converting String to Date
     */
    public void testDateTransform() {
        final String DATE = "2009-11-16T05:30:30Z";
        final String xml = "<string_to_date>" + DATE + "</string_to_date>";
        try {
            Property2DateTransformer transformer = new Property2DateTransformer();
            Date date = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(date);
        } catch (Fabric3Exception te) {
            fail("Transform exception should not occur " + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test failure converting String to Date
     */
    public void testDateTransformFailure() {
        final String WRONG_DATE = "20/2007/01";
        final String xml = "<string_to_date>" + WRONG_DATE + "</string_to_date>";
        try {
            Property2DateTransformer transformer = new Property2DateTransformer();
            transformer.transform(getNode(xml), getClass().getClassLoader());
            fail("Should not reach here something wrong in [ String2Date ] code");
        } catch (Fabric3Exception te) {
            assertNotNull(te);
            assertTrue(ParseException.class.isAssignableFrom(te.getCause().getClass()));
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

 
}
