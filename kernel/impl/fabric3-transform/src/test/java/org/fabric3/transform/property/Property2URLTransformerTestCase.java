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

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.spi.transform.TransformationException;


/**
 * Tests String to URL transformation.
 *
 * @version $Rev$ $Date$
 */
public class Property2URLTransformerTestCase extends BaseTransformTest {

    /**
     * Test for successful transformation from String to URL
     */
    public void testURLTransformSuccess() {
        final String urlContent = "ftp://testf3.org";
        final String xml = "<string_to_url>" + urlContent + "</string_to_url>";

        try {
            Property2URLTransformer transformer = new Property2URLTransformer();
            final URL transformedURL = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(transformedURL);
        } catch (TransformationException te) {
            fail("TransformationException : - Should Not Occur" + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test for unsuccessful Conversion from String URL
     */
    public void testURLTransformationSuccess() {
        final String erroredURL = "failedURL";
        final String xml = "<string_to_urlerror>" + erroredURL + "</string_to_urlerror>";

        try {
            Property2URLTransformer transformer = new Property2URLTransformer();
            transformer.transform(getNode(xml), getClass().getClassLoader());
            fail("Should not convert to URL");
        } catch (TransformationException te) {
            assertNotNull(te);
            MalformedURLException.class.isAssignableFrom(te.getCause().getClass());
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }


}
