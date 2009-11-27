/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.transform.property;

import java.text.ParseException;
import java.util.Date;

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Date transform.
 *
 * @version $Rev$ $Date$
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
        } catch (TransformationException te) {
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
        } catch (TransformationException te) {
            assertNotNull(te);
            assertTrue(ParseException.class.isAssignableFrom(te.getCause().getClass()));
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

 
}
